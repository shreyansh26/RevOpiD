import re
import pickle


words2num = {}

f = open('noun_phrase2.txt', 'r')

words_list = []

for line in f:
    word_list = line.strip().split(' ')
    for i in word_list:
        words_list.append(i)

f.close()
words_list = set(words_list)

i = 1

for word in words_list:
    words2num[word] = i
    i += 1

with open('words2num_dict.pickle', 'wb') as handle:
    pickle.dump(words2num, handle, protocol=pickle.HIGHEST_PROTOCOL)


with open('words2num_dict.pickle', 'rb') as handle:
    words2num = pickle.load(handle)

num_list = []

f = open('noun_phrase2.txt', 'r')
for line in f:
    word_list = line.strip().split(' ')
    num_line_list = []
    for i in word_list:
        num_line_list.append(words2num[i])
    
    num_line_list = list(set(num_line_list))
    num_line_list.sort()
    num_list.append(num_line_list)

f.close()

size_dict = len(words2num)

f = open('num_phrase.txt', 'w')

for idx, num_l in enumerate(num_list):
    for j in num_l:
        f.write(str(j))
        f.write(' ')
    f.write(str(size_dict+5))
    if(idx != len(num_list)-1):
        f.write('\n')

f.close()