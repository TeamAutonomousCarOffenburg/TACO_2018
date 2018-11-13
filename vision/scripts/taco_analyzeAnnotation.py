import json
import os
import argparse
import taco_tools as tt
from matplotlib import pyplot as plt

data = {}
num_of_annotation = 0
num_of_image = 0


def analyze_annotation(path, json_files):
    for file in json_files:
        json_data = tt.read_json(path + file)
        global num_of_image
        if type(json_data) is dict:
            num_of_image = len(json_files)
            annotation_data = json_data['annotations']
            count_annotation(annotation_data)

        elif type(json_data) is list:
            num_of_image = len(json_data)
            for file_data in json_data:
                annotation_data = file_data['annotations']
                count_annotation(annotation_data)

    print("num_of_images: " + str(num_of_image))
    print("num_of_annotations: " + str(num_of_annotation))
    print("data: " + str(data))

    show_hist_bar()
    show_hist_pie()


def count_annotation(annotation_data):
    global num_of_annotation
    num_of_annotation += len(annotation_data)
    for obj in annotation_data:
        class_name = obj['class']
        if class_name in data:
            data[class_name] = data[class_name] + 1
        else:
            data[class_name] = 1


def show_hist_bar():
    fig, ax = plt.subplots()
    rect = ax.bar(data.keys(), data.values(), align='center')
    auto_label(ax, rect)
    plt.title('object occurrences')
    plt.xticks(rotation='vertical')
    plt.ylabel('occurrences')
    plt.xlabel('class')
    plt.show()

def show_hist_pie():
    explode = (0.1, 0, 0, 0.1,0,0,0,0,0.1,0,0.1,0,0,0,0)
    fig1, ax1 = plt.subplots()
    ax1.pie(data.values(), explode=explode, labels=data.keys(), autopct='%1.1f%%', startangle=90)
    ax1.axis('equal')  # Equal aspect ratio ensures that pie is drawn as a circle.
    plt.show()

def auto_label(ax, rects):
    """
    Attach a text label above each bar displaying its height
    """
    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x() + rect.get_width()/2., 1.05*height,
                '%d' % int(height),
                ha='center', va='bottom')


def get_args():
    parser = argparse.ArgumentParser(description='Analyzes annotation files and shows the distribution for each label')
    parser.add_argument('-sd', '--source-dir',
                        required=True,
                        help='source directory with all annotations files that should analyzed')
    parser.add_argument('-sf', '--source-file',
                        required=False,
                        help="one annotation file from source dir to analyze")
    args = parser.parse_args()

    # append the trailing slash if needed
    args.source_dir = os.path.join(args.source_dir, '')

    if not os.path.isdir(args.source_dir):
            raise NotADirectoryError("source-dir ist not a valid directory")
    elif args.source_file is not None:
        if not os.path.isfile(args.source_dir + args.source_file):
            raise FileNotFoundError("SOURCE FILE NOT FOUND")

    return args


def main():

    args = get_args()

    json_files = [x for x in os.listdir(args.source_dir) if x.endswith(".json")]

    if args.source_file is not None:
        json_files = [file for file in json_files if file == args.source_file]
    print('FilesCount: ' + str(len(json_files)))
    analyze_annotation(args.source_dir, json_files)


main()


