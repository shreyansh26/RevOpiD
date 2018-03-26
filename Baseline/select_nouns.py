import pickle
import re
import pandas as pd

FILE_NAME = '0_AutomotiveProd3'

nouns_list = ['suction', 'mechanism', 'puller', 'grip', 'dent', 'diameter', 'cup', "it's", "i'm", 'cup', 'money', 'product', 'quality', 'work', 'size', 'job', 'item', 'rubber', 'tool', 'metal']

with open('positive_dict.pickle', 'rb') as handle:
    positive_dict = pickle.load(handle)

with open('negative_dict.pickle', 'rb') as handle:
    negative_dict = pickle.load(handle)

with open('neutral_dict.pickle', 'rb') as handle:
    neutral_dict = pickle.load(handle)

noun_list = []

for x in positive_dict:
	noun_list.append(x)
for x in negative_dict:
	noun_list.append(x)

noun_list = list(set(noun_list))

f = open('cleaned_data_for_pruning.txt', 'r')

sentences = []

for l in f:
	se = re.sub("[^\w\d'\s.]+",'',l)
	x = se.strip().split('.')
	for i in x:
		sentences.append(i.strip())


words_in_sentence = []
for i in range(len(sentences)):
	if sentences[i] != '' or sentences[i] != '\n':
		sentence_word = sentences[i].split(' ')
		sentence_word = list(filter(None, sentence_word))
	if sentence_word != []:
		words_in_sentence.append(sentence_word)

f.close()
count_list = []
for i in noun_list:
	count = 0
	for sent in words_in_sentence:
		count += sent.count(i)
	count_list.append(count)

sorted_noun_list = [i[0] for i in sorted(zip(noun_list, count_list), key=lambda l: l[1], reverse=True)]
count2_simple = sorted(count_list, reverse=True)

selected_nouns = sorted_noun_list[:20]

a = set(selected_nouns) & set(nouns_list)

'''
print("Precision:", len(a)/len(selected_nouns))
print("Recall:", len(a)/len(nouns_list))

for i in selected_nouns:
	print(i)'''

noun_list_without_one = []
noun_list_without_two = []

for x in positive_dict:
	if x in selected_nouns:
		count = 0
		for i in positive_dict[x]:
			for sent in words_in_sentence:
				if set(i[0][0])< set(sent) and set(i[0][1])< set(sent):
					count += 1
		'''print(x)
		print(positive_dict[x])
		print(count)
		print()'''
		if count > 1:
			noun_list_without_one.append(x)
		if count > 2:
			noun_list_without_two.append(x)
	    	

for x in negative_dict:
	if x in selected_nouns:
		count = 0
		for i in negative_dict[x]:
			for sent in words_in_sentence:
				if set(i[0][0])< set(sent) and set(i[0][1])< set(sent):
					count += 1
		'''print(x)
		print(negative_dict[x])
		print(count)
		print()'''
		if count > 1:
			noun_list_without_one.append(x)
		if count > 2:
			noun_list_without_two.append(x)

for x in neutral_dict:
	if x in selected_nouns:
		count = 0
		for i in neutral_dict[x]:
			for sent in words_in_sentence:
				if set(i[0][0])< set(sent) and set(i[0][1])< set(sent):
					count += 1
		'''print(x)
		print(neutral_dict[x])
		print(count)
		print()'''
		if count > 1:
			noun_list_without_one.append(x)
		if count > 2:
			noun_list_without_two.append(x)

'''
print(len(feature_list))
print(len(feature_list_without_neutral))
print(len(feature_list_without_one))
print(len(feature_list_without_two))'''
'''for x in positive_dict:
	if x in selected_nouns:
		print(x)
		print(positive_dict[x])
		print(len(positive_dict[x]))

for x in negative_dict:
	if x in selected_nouns:
		print(x)
		print(negative_dict[x])
		print(len(negative_dict[x]))

for x in neutral_dict:
	if x in selected_nouns:
		print(x)
		print(neutral_dict[x])
		print(len(neutral_dict[x]))'''

f = open('cleaned_data_for_pruning.txt', 'r')

sentences = []
review_sentences = []
for l in f:
	se = re.sub("[^\w\d'\s.]+",'',l)
	x = se.strip()
	x = re.sub('\.', '', x)
	sentences.append(x)

f.close()

opinion_matrix = []
opinion_matrix_without_one = []
opinion_matrix_without_two = []

## OPINION MATRIX FULL
for i, rev in enumerate(sentences):
	matrix_row = []
	for x in positive_dict:
		if x in selected_nouns:
			flag = 0
			for li in positive_dict[x]:
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	for x in negative_dict:
		if x in selected_nouns:
			flag = 0
			for li in negative_dict[x]:
				#print(rev)
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	for x in neutral_dict:
		if x in selected_nouns:
			flag = 0
			for li in neutral_dict[x]:
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	#print(len(matrix_row))
	opinion_matrix.append(matrix_row)

## OPINION MATRIX WITHOUT ONE
for i, rev in enumerate(sentences):
	matrix_row = []
	for x in positive_dict:
		if x in noun_list_without_one:
			flag = 0
			for li in positive_dict[x]:
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	for x in negative_dict:
		if x in noun_list_without_one:
			flag = 0
			for li in negative_dict[x]:
				#print(rev)
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	for x in neutral_dict:
		if x in noun_list_without_one:
			flag = 0
			for li in neutral_dict[x]:
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	#print(len(matrix_row))
	opinion_matrix_without_one.append(matrix_row)

## OPINION MATRIX WITHOUT TWO
for i, rev in enumerate(sentences):
	matrix_row = []
	for x in positive_dict:
		if x in noun_list_without_two:
			flag = 0
			for li in positive_dict[x]:
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	for x in negative_dict:
		if x in noun_list_without_two:
			flag = 0
			for li in negative_dict[x]:
				#print(rev)
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	for x in neutral_dict:
		if x in noun_list_without_two:
			flag = 0
			for li in neutral_dict[x]:
				if set(li[0][0]) < set(rev.split(' ')) and set(li[0][1]) < set(rev.split(' ')):
					flag = 1
					break
			if flag == 1:
				matrix_row.append(1)
			else:
				matrix_row.append(0)
	#print(len(matrix_row))
	opinion_matrix_without_two.append(matrix_row)

f = open('id_'+FILE_NAME+'.txt', 'r')
reviewer_id = []
for l in f:
	reviewer_id.append(l.strip())
f.close()

f = open(FILE_NAME+'_opinion_matrix.csv', 'w')
for ind, i in enumerate(opinion_matrix):
	f.write(reviewer_id[ind]+',')
	for j in range(len(i)-1):
		f.write(str(i[j])+',')
	f.write(str(i[len(i)-1]))
	f.write('\n')

f = open(FILE_NAME+'_opinion_matrix_without_one.csv', 'w')
for ind, i in enumerate(opinion_matrix_without_one):
	f.write(reviewer_id[ind]+',')
	for j in range(len(i)-1):
		f.write(str(i[j])+',')
	f.write(str(i[len(i)-1]))
	f.write('\n')

f = open(FILE_NAME+'_opinion_matrix_without_two.csv', 'w')
for ind, i in enumerate(opinion_matrix_without_two):
	f.write(reviewer_id[ind]+',')
	for j in range(len(i)-1):
		f.write(str(i[j])+',')
	f.write(str(i[len(i)-1]))
	f.write('\n')