import re
import pickle

with open('compactness_pruned_feature_list.pickle', 'rb') as handle:
    compactness_pruned_feature_list = pickle.load(handle)

feature_list = compactness_pruned_feature_list[:]

not_include = []

for i in range(len(feature_list)-1):
	count = 0
	for j in range(i+1, len(feature_list)):
		if set(feature_list[i][0]) < set(feature_list[j][0]):
			count += feature_list[j][1]
	rem_count = feature_list[i][1] - count
	if rem_count < 2:
		not_include.append(i)

redundancy_pruned_feature_list = [i for j, i in enumerate(feature_list) if j not in not_include]
'''
for i in redundancy_pruned_feature_list:
	print(i)'''

with open('redundancy_pruned_feature_list.pickle', 'wb') as handle:
    pickle.dump(redundancy_pruned_feature_list, handle)
