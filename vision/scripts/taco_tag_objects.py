import argparse
import glob
import os
import time

import cv2

import numpy as np

from utils import prepare_ground_truth as gt_tools
from utils import tools
from utils import visualisation as v
from utils.tf_object_detection_eval import ObjectDetection


extensions = ['jpg', 'jpeg', 'JPG', 'JPEG', 'png', 'PNG']

def get_args():
    parser = argparse.ArgumentParser(
        description='Auto tag labels in provided images. Default treshold 50%')
    parser.add_argument(
        '-c',
        '--config',
        required=False,
        default="./config/vision_server.config.json",
        help=
        "Path to the configuration file with the settings for Object Detection"
    )
    parser.add_argument('--image-dir', required=True)
    parser.add_argument('--output-dir', required=True)
    parser.add_argument('-iw', '--image-width', type=int, default=1240)
    parser.add_argument('-ih', '--image-height', type=int, default=330)
    parser.add_argument('-ic', '--image-channels', type=int, default=3)
    parser.add_argument(
        '-v',
        '--visualize',
        action='store_true',
        help="Displays the images with the results")
    parser.add_argument(
        '-gu',
        '--gpu-usage',
        type=float,
        default=0.75,
        help="How much gpu can we use?")
    parser.add_argument(
        '-t',
        '--threshold',
        type=float,
        default=0.5,
        help='Threshold for detected objects')
    parser.add_argument('-img',
                        '--save-images',
                        default=0,
                        required=False,
                        help="save predicted images in output dir")
    parser.add_argument('-bb',
                        '--bounding-box',
                        required=False,
                        default=0)
    args = parser.parse_args()

    if not os.path.isfile(args.config):
        print("[ERROR] config file does not exist... {}".format(args.config))
        exit(1)
    return args



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
    args = get_args()

    validate_images = []

    for extension in extensions:
        for file in glob.glob(args.image_dir + '/*.' + extension):
            validate_images.append(file)

    # remove duplicate images from image_dir.
    # maybe because of same upper and lowercase extension
    validate_images = list(set(validate_images))
    validate_images.sort()


    config = tools.load_config(args.config)
    od = ObjectDetection(config, dict(), args.image_width, args.image_height, image_annotation=True,
                         image_visualization=args.bounding_box,gpu_usage=args.gpu_usage, threshold=args.threshold).start_up()


    images = list()
    for image in validate_images:
        images.append(os.path.basename(image))

    numpy_array = read_images(validate_images)

    result = od.detect_objects(numpy_array, images)


    if args.output_dir is not None and args.save_images:
        save_result_images(result['result_images'], args.output_dir, images)

    if args.output_dir is None:
        args.output_dir = args.image_dir
    save_result_annotation(result['result_annotation'], images, args.output_dir, "result_annotations.json")



    od.shutdown()



