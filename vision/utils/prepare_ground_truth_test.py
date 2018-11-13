
from utils import prepare_ground_truth as gt_tools
from utils import tools
# TODO proper TestCase

inputfile = 'c:\\tmp\\eval_real_new\\final_config.json'

json_data = tools.read_json_file(inputfile)

gt_dict = gt_tools.prepare_ground_truth(json_data)

print

print(str(gt_dict))
