import tensorflow as tf
import numpy as np
from object_detection.utils import label_map_util
import object_detection.eval_util as eval_util
import object_detection.utils.visualization_utils as vis_util
class ObjectDetection:
    def __init__(self,
                 config,
                 groundtruth = dict(),
                 im_width = 1240,
                 im_height = 330,
                 image_visualization=False,
                 image_annotation=False,
                 gpu_usage=0.75,
                 threshold=0.5):
        self.category_index = {}
        self.config = {}
        self.tf_session = None
        self.detection_graph = None
        self.image_visualization = image_visualization
        self.image_annotation = image_annotation
        self.im_width = im_width
        self.im_height = im_height
        self.config = config
        self.groundtruth = groundtruth
        self.gpu_usage = gpu_usage
        self.threshold = threshold
        self.categories = None
    def start_up(self):
        print("[OBJECT DETECTION] starting {} Graph".format(
            self.config['model_name']))
        label_map = label_map_util.load_labelmap(self.config["label_map"])
        self.categories = label_map_util.convert_label_map_to_categories(
            label_map,
            max_num_classes=self.config["num_classes"],
            use_display_name=True)
        self.category_index = label_map_util.create_category_index(self.categories)
        self.detection_graph = tf.Graph()
        with self.detection_graph.as_default():
            od_graph_def = tf.GraphDef()
            with tf.gfile.GFile(self.config["path_to_ckpt"], 'rb') as fid:
                serialized_graph = fid.read()
                od_graph_def.ParseFromString(serialized_graph)
                tf.import_graph_def(od_graph_def, name='')
                config = tf.ConfigProto()
                config.gpu_options.per_process_gpu_memory_fraction = self.gpu_usage
            self.tf_session = tf.Session(
                config=config, graph=self.detection_graph)
        print("[OBJECT DETECTION] started")
        return self
    def shutdown(self):
        self.tf_session.close()
        print("[OBJECT DETECTION] stopped")
    def detect_objects(self, image, image_id):
        """
        :param image: numpy array with shape [N, width, height, 3 (color)]
        :param image_id:
        :return: returns a result dictionary with entries that depends on the program args
        """
        # Expand dimensions since the model expects images to have shape: [N, None, None, 3]
        np_images = image
        image_tensor = self.detection_graph.get_tensor_by_name('image_tensor:0')
        boxes_tensor = self.detection_graph.get_tensor_by_name('detection_boxes:0')
        scores_tensor = self.detection_graph.get_tensor_by_name('detection_scores:0')
        classes_tensor = self.detection_graph.get_tensor_by_name('detection_classes:0')
        num_detections_tensor = self.detection_graph.get_tensor_by_name('num_detections:0')
        # Actual detection.
        boxes = list()
        scores = list()
        classes = list()
        # TODO maybe evaluate more than one image at each run
        num_img = 0
        max_num_img = np.size(np_images, 0)
        for image_idx in range(np.size(np_images, 0)):
            num_img+=1
            (boxes_tmp, scores_tmp, classes_tmp, num_detections) = self.tf_session.run(
                [boxes_tensor, scores_tensor, classes_tensor, num_detections_tensor],
                feed_dict={image_tensor: np.expand_dims(np_images[image_idx], 0)})
            boxes.append(np.squeeze(boxes_tmp))
            scores.append(np.squeeze(scores_tmp))
            classes.append(np.squeeze(classes_tmp))
            print("Image {} of {} processed".format(num_img, max_num_img))
        boxes = np.array(boxes)
        scores = np.array(scores)
        classes = np.array(classes)
        result_dict = dict()

        if len(result_dict) > 0:
            result_dict['result_metric'] = self.evaluate_result(
                image_id,
                boxes,
                scores,
                classes
            )
        else:
            result_dict['result_metric'] = {}
        if self.image_visualization >= 0:
            result_dict['result_images'] = self.prepare_result_images(
                np_images,
                boxes,
                scores,
                classes
            )
        if self.image_annotation:
            result_dict['result_annotation'] = self.prepare_result_annotation(
                boxes,
                scores,
                classes
            )
        return result_dict
    def evaluate_result(self, image_ids, boxes, scores, classes):
        groundtruth_boxes, groundtruth_classes = self.get_normalized_groundtruth(image_ids)
        """
        Prepare the result list for the evaluation process from the object detection api
        'image_id': a list of string ids
        'detection_boxes': a list of float32 numpy arrays of shape [N, 4]
        'detection_scores': a list of float32 numpy arrays of shape [N]
        'detection_classes': a list of int32 numpy arrays of shape [N]
        'groundtruth_boxes': a list of float32 numpy arrays of shape [M, 4]
        'groundtruth_classes': a list of int32 numpy arrays of shape [M]
        """
        result_list = {'image_id': image_ids,
                       'detection_boxes': boxes,
                       'detection_scores': scores,
                       'detection_classes': classes,
                       'groundtruth_boxes': groundtruth_boxes,
                       'groundtruth_classes': groundtruth_classes}
        metric = eval_util.evaluate_detection_results_pascal_voc(
            result_lists=result_list,
            categories=self.categories,
            label_id_offset=1
        )
        return metric
    #
    def prepare_result_images(self, np_images, boxes, scores, classes):
        result_images = list()
        # Filter all Boxes with a Score >= 0.5, for bounding boxes.
        # In visualize_boxes_and_labels_on_image_array function this threshold is build in as param
        if self.image_visualization == 1:
            filtered_boxes_per_image = []
            for image_idx in range(np.size(np_images, 0)):
                filtered_boxes = []
                for score_idx, score in enumerate(scores[image_idx]):
                    if score >= self.threshold:
                        filtered_boxes.append(boxes[image_idx][score_idx])
                filtered_boxes_per_image.append(np.array(filtered_boxes))
        for index in range(np.size(np_images, 0)):
            image_np = np_images[index]
            # Draw bounding boxes in image
            if self.image_visualization == 1:
                vis_util.draw_bounding_boxes_on_image_array(
                    image_np,
                    filtered_boxes_per_image[index],
                    thickness=4,
                )
            # Draw bounding boxes and scores in image
            if self.image_visualization == 0:
                vis_util.visualize_boxes_and_labels_on_image_array(
                    image_np,
                    np.squeeze(boxes[index]),
                    np.squeeze(classes[index]).astype(np.int32),
                    np.squeeze(scores[index]),
                    self.category_index,
                    use_normalized_coordinates=True,
                    line_thickness=4,
                    agnostic_mode=False)
            result_images.append(image_np)
        return result_images
    #
    def prepare_result_annotation(self, boxes, scores, classes):
        """
        :param boxes: result boxes from the detection, list of numpy array of shape [N,4]
        :param scores: result scores from the detection list of numpy array of shape [N]
        :param classes: result classes from the detection list of numpy array of shape [N]
        :return: returns a list of list of annotations for each image, with the detection results.
        Can be stored as a json file.
        """
        result_annotation = list()
        for image_idx in range(np.size(boxes, 0)):
            annotation_list = list()
            for box_idx in range(np.size(boxes, 1)):
                if scores[image_idx][box_idx] > self.threshold:
                    # TODO is this check required ?
                    if classes[image_idx][box_idx] in self.category_index.keys():
                        class_name = self.category_index[classes[image_idx][box_idx]]['name']
                        ymin, xmin, ymax, xmax = tuple(boxes[image_idx][box_idx])
                        ymin = int(ymin * self.im_height)
                        xmin = int(xmin * self.im_width)
                        ymax = int(ymax * self.im_height)
                        xmax = int(xmax * self.im_width)
                        annotations = dict()
                        annotations['class'] = class_name
                        annotations['x'] = str(int(xmin))
                        annotations['y'] = str(int(ymin))
                        annotations['width'] = str(int(xmax - xmin))
                        annotations['height'] = str(int(ymax - ymin))
                        # TODO score usefull in annotation file ?
                        # annotations['score'] = scores[image_idx][box_idx]
                        annotation_list.append(annotations)
            result_annotation.append(annotation_list)
        return result_annotation
    def get_normalized_groundtruth(self, images):
        """
        :param images:
        :return: Returns a list of numpy arrays of boxes [N, 4]
        and a corresponding list of numpy arrays of class_ids [N]
        """
        groundtruth_boxes = list()
        groundtruth_classes = list()
        FP = 0
        for image in images:
            if image not in self.groundtruth:
                continue
            gt_box_per_image = list()
            gt_class_per_image = list()
            for index, label in enumerate(self.groundtruth[image]['groundtruth_labels']):
                for cat in self.categories:
                    if label == cat['name']:
                        gt_class_per_image.append(cat['id'])
                        gt_box_per_image.append(self.groundtruth[image]['groundtruth_boxes'][index])
            groundtruth_classes.append(np.array(gt_class_per_image))
            groundtruth_boxes.append(np.array(gt_box_per_image))
        groundtruth_boxes = np.array(groundtruth_boxes)
        groundtruth_classes = np.array(groundtruth_classes)
        return groundtruth_boxes, groundtruth_classes