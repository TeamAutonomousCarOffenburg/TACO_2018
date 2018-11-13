import json
import os
import argparse
from object_detection.utils import label_map_util

def get_args():
    parser = argparse.ArgumentParser(description='Removes all annotations from source file, which are not listed in labels file')
    parser.add_argument('-s', '--source',
                        required=True,
                        help='source file')
    parser.add_argument('-l', '--labels',
                        required=True,
                        help='labels to keep file (.pbtxt)')

    args = parser.parse_args()

    if not os.path.isfile(args.source):
        raise FileNotFoundError("Source File doesn't exit")
    if not os.path.isfile(args.labels):
        raise FileNotFoundError("labels File doesn't exit")

    return args


def main():

    args = get_args()


    label_map_dict = label_map_util.get_label_map_dict(args.labels)
    with open(args.source) as data_file:
        data = json.load(data_file)

    print("Labels: {}".format(label_map_dict.keys()))
    annotations_removed = 0
    for image in data:
        lenPre = len(image["annotations"])
        image['annotations'] = [annotation for annotation in image['annotations']
                                if annotation["class"] in label_map_dict.keys()]
        annotations_removed += lenPre - len(image["annotations"])


    print('annotations removed: {}'.format(annotations_removed))

    with open(args.source, 'w') as outfile:
        json.dump(data, outfile)


main()


