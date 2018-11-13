import argparse
import os

import taco_tools as tt


def get_args():
    parser = argparse.ArgumentParser(description='Merges annotation files to one final list')
    parser.add_argument('-s', '--source-dir',
                        required=True,
                        help='source directory with all annotations files that should merged to one')
    parser.add_argument('-tj', '--target-json',
                        required=False,
                        default="\\final_config.json",
                        help="filename for target json file, which will generated in source-dir")
    args = parser.parse_args()

    # append the trailing slash if needed
    args.source_dir = os.path.join(args.source_dir, '')

    if not os.path.isdir(args.source_dir):
        raise NotADirectoryError("source-dir ist not a valid directory")
    else:
        if os.path.isfile(args.source_dir + args.target_json):
            raise FileExistsError("targe-json already exists")
    return args


def collect_json_annotations(path_to_files, json_files):

    json_content = list()
    for file_name in json_files:
        json_data = tt.read_json(path_to_files + file_name)

        # if the file is already a list if annotations
        if type(json_data) == list:
            json_content += json_data
        else:
            json_content.append(json_data)

    return json_content


def main():

    args = get_args()

    json_files = [file for file in os.listdir(args.source_dir) if file.endswith('.json')]

    print("OUTPUT_FILE: " + args.source_dir + args.target_json)
    print("INPUT_FILES: " + str(len(json_files)))

    json_data = collect_json_annotations(args.source_dir, json_files)

    print(len(json_data))

    tt.write_json(args.source_dir + args.target_json, json_data)


if __name__ == '__main__':
    main()
