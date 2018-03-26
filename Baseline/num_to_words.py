import re
import pickle

with open('words2num_dict.pickle', 'rb') as handle:
    words2num = pickle.load(handle)

num2words = {v: k for k, v in words2num.items()}

freq_raw = []

with open('output.txt') as f:
    for i in range(31):
        f.readline()
    for line in f:
    	line = re.sub('\[\w+\]', '', line)
    	line = re.sub('\n', '', line)
    	line = re.sub('{', '', line)
    	line = re.sub('}', '', line)
    	freq_raw.append(line.strip())

num_word_list = []
freq_list = []
for i in range(len(freq_raw)):
	if freq_raw[i] != '':
		num_list = freq_raw[i].split(' ')
		#print(num_list)
		for j in range(len(num_list)-2):
			num_list[j] = int(num_list[j])
		num_list[-1] = int(num_list[-1])
		#print(num_list[-1])
		num_word_list.append(num_list[:-2])
		freq_list.append(num_list[-1])

words_tuple = []
for i in range(len(num_word_list)):
	words = []
	for j in range(len(num_word_list[i])):
		if(num_word_list[i][j] != len(num2words)+5):
			words.append(num2words[num_word_list[i][j]].strip())
	words_tuple.append(tuple((words, freq_list[i])))
'''
for i in words_tuple:
	print(i)
'''
with open('words_tuple.pickle', 'wb') as handle:
    pickle.dump(words_tuple, handle)