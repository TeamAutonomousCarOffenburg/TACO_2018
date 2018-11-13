import argparse
import glob
import os
import time

import cv2

import numpy as np

from utils import prepare_ground_truth as gt_tools
from utils import tools
from utils import visualisation as v
from utils.tf_object_detection import ObjectDetection


extensions = ['jpg', 'jpeg', 'JPG', 'JPEG', 'png', 'PNG']


def get_args():
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('--image-dir', required=True)
    parser.add_argument('--output-dir', required=True)
    parser.add_argument('-bb',
                        '--bounding-box',
                        required=False,
                        default=0)
    parser.add_argument('--validate-json', required=True)
    parser.add_argument('-g', '--graph', required=False)

    parser.add_argument('--result-json',
                        default="result_detection.json",
                        required=False)
    parser.add_argument(
        '-c',
        '--config',
        required=False,
        # default="./config/vision_server.config.json",
        help="Path to the configuration file with the settings for Object Detection"
    )
    parser.add_argument(
        '-mn',
        '--model-name',
        required=False,
        default="Model_name",
        help="The name of the used Model")

    parser.add_argument(
        '-nc',
        '--num-classes',
        required=False,
        help="How many clases are in mapping-file")

    parser.add_argument(
        '-mf',
        '--mapping-file',
        required=False,
        help="Path to the Mapping File, eg. data/aadc_labels.pbtxt")

    parser.add_argument('-iw', '--image-width', type=int, default=1240)
    parser.add_argument('-ih', '--image-height', type=int, default=330)
    parser.add_argument('-ic', '--image-channels', type=int, default=3)
    parser.add_argument(
        '-gu',
        '--gpu-usage',
        type=float,
        default=0.5,
        help="How much gpu can we use?")

    args = parser.parse_args()

    if not os.path.isdir(args.image_dir):
        print("[ERROR] Image dir does not exist... {}".format(args.image_dir))

    if os.path.isdir(args.output_dir):
        for file in os.scandir(args.output_dir):
            os.unlink(file.path)
    else:
        os.makedirs(args.output_dir)

    if args.bounding_box is not None:
        args.bounding_box = int(args.bounding_box)

    if args.config is not None or \
       args.mapping_file is None and \
       args.num_classes is None and \
       args.graph is None:
        raise ValueError("neither config_file or needed arguments for object detection given")

    return args


def main():
    args = get_args()
    validate_images = []

    for extension in extensions:
        for file in glob.glob(args.image_dir + '*.' + extension):
            validate_images.append(file)

    # remove duplicate images from image_dir.
    # maybe because of same upper and lowercase extension
    validate_images = list(set(validate_images))
    validate_images.sort()

    validation_config = tools.read_json_file(args.validate_json)
    config = dict()
    if args.config is not None:
        config = tools.load_config(args.config)
    else:
        config["path_to_ckpt"] = args.graph
        config["label_map"] = args.mapping_file
        config["num_classes"] = int(args.num_classes)
        config["model_name"] = args.model_name

    config_images = {}
    for image in validation_config:
        config_images[image["filename"]] = image["annotations"]

    if len(config_images) == 0:
        print("no images found in config...")
        exit()

    gt_dict = gt_tools.prepare_ground_truth(
        validation_config,
        im_height=args.image_height,
        im_width=args.image_width)

    od = ObjectDetection(config,
                         gt_dict,
                         args.image_width,
                         args.image_height,
                         image_annotation=args.result_json is not None,
                         image_visualization=args.bounding_box,
                         gpu_usage=args.gpu_usage)
    od.start_up()

    images = list()
    for image in validate_images:
        images.append(os.path.basename(image))

    numpy_array = read_images(validate_images)

    result = od.detect_objects(numpy_array, images)

    print('METRIC: {} '.format(result['result_metric']))

    if args.result_json is not None:
        save_result_annotation(result['result_annotation'], images, args.image_dir, args.result_json)

    if args.output_dir is not None:
        save_result_images(result['result_images'], args.output_dir, images)

    od.shutdown()


def single_evaluation(object_detection, validate_images, config_images, args):

    """
    Old Validation Process.
    TODO: Can be removed.

    Not usable metric, checks only if the detected label is in the ground_truth.

    Now NOT usable because of changed return from od.detect_object method.
    """

    total_classes_in_config = 0
    predicted_valid = 0
    predicted_false = 0
    prediction_time = 0
    validated_images = 0
    empty_result = 0

    for image in validate_images:
        image_file = os.path.basename(image)

        if image_file not in config_images.keys():
            # print("Skip image {} cause not in config...".format(image_file))
            continue
        else:
            validated_images += 1

        imgcv = cv2.imread(image)
        start = time.time()
        result = object_detection.detect_objects(imgcv, image_file, image)
        end = time.time()

        total_classes_in_config += len(config_images[image_file])

        if len(result["recognizedObjects"]) > 0:
            objects = result["recognizedObjects"]
            prediction_time += (end - start)

            i = 0
            for tmp_object in objects:
                source_image = imgcv.copy()
                result_image = os.path.join(args.output_dir,
                                        "result_" + str(i) + "_" + image_file)
                i += 1
                tmp = list()
                tmp.append(tmp_object)
                tmp_image = v.draw_boxes_and_labels(source_image,
                                            tmp,
                                            thickness=2)
                cv2.imwrite(result_image, tmp_image)

            # METRIC
            for object in objects:
                if result_in_annotations(config_images[image_file],
                                         object["class_name"]):
                    predicted_valid += 1
                    # print("valid: {}".format(image_file))
                else:
                    predicted_false += 1
                    print("{} predicted not tagged class: {}".format(
                        image_file, object["class_name"]))
        else:
            empty_result += 1

    print(
        "validation classes: {} valid: {} false: {} empty results: {} total: {:.2f}% avg_time {:.2f}s".
        format(total_classes_in_config, predicted_valid, predicted_false,
               empty_result, predicted_valid / total_classes_in_config * 100,
               prediction_time / validated_images))


def result_in_annotations(annotations, result_class):
    for annotation in annotations:
        if result_class == annotation["class"]:
            return True

    return False


def read_images(all_images):
    """
    ;param all_images: a list with all images to load

    :return nympy array with all images [N, None, None, 3]
   """
    image_list = list()
    for image in all_images:
        image_list.append(cv2.imread(image))
    return np.array(image_list)


def save_result_annotation(result_annotation, image_files,  output_dir, target_file):
    """
    :param result_annotation:
    :param image_files:
    :param output_dir:
    :param target_file:

    """

    json_data = list()
    result_file = os.path.join(output_dir, target_file)
    for idx, annotation in enumerate(result_annotation):

        tmp = dict()
        tmp['annotations'] = annotation
        tmp['class'] = 'image'
        tmp['filename'] = image_files[idx]

        json_data.append(tmp)

    tools.write_json(result_file, json_data)


def save_result_images(np_images, output_dir, image_files):
    """
    :param np_images: list of numpy images that are modified with bounding boxes
    or bounding boxes and labels.
    :param output_dir: The directory in which the images are saved
    :param image_files:
    """
    for idx, np_image in enumerate(np_images):
        result_image = os.path.join(output_dir, "result_" + image_files[idx])
        cv2.imwrite(result_image, np_image)


if __name__ == '__main__':
    main()
