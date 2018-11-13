

def prepare_ground_truth(json_data, im_height=1 , im_width=1):

    """
    :param json_data: The json file that holds the ground truth content

    :return A dictionary with the filenames from the json_data as keys
    and the corresponding and prepared annotation data.
    """

    gt_dict = dict()

    for element in json_data:
        filename = element['filename']
        gt = element['annotations']

        boxes = []
        labels = []
        for annotation in gt:
            class_label = annotation['class']
            xmin = annotation['x']
            ymin = annotation['y']
            ymax = ymin + annotation['height']
            xmax = xmin + annotation['width']

            # normalize the coordinates if needed (im_width, im_height)
            ymin = ymin / im_height
            xmin = xmin / im_width
            ymax = ymax / im_height
            xmax = xmax / im_width

            labels.append(class_label)
            boxes.append([ymin, xmin, ymax, xmax])

        gt_dict[filename] = {'groundtruth_labels': labels,
                             'groundtruth_boxes': boxes}

    return gt_dict
