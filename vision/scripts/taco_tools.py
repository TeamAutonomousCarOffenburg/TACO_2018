import json


def write_json(json_file, data):
    # print("WRITE TO: {}".format(json_file))
    with open(json_file, 'w') as out_json:
        json.dump(data, out_json, indent=4)


def read_json(json_file):
    # print("READ FILE: {}".format(json_file))
    with open(json_file) as json_data:
        json_content = json.load(json_data)
    return json_content

