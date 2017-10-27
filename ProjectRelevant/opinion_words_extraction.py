import re
import pickle
from nltk.corpus import wordnet as wn
from nltk import pos_tag
from collections import OrderedDict
from  more_itertools import unique_everseen

with open('redundancy_pruned_feature_list.pickle', 'rb') as handle:
    redundancy_pruned_feature_list = pickle.load(handle)

#print(redundancy_pruned_feature_list)
opinion_features_and_words = []

for i in redundancy_pruned_feature_list:
	adj_list = []
	noun_list = []
	for j, word in enumerate(i[0]):
		if re.match("^[a-zA-Z]*$", word):
			#print(word, pos_tag([word]))
			if pos_tag([word])[0][1] == 'JJ': # or pos_tag([word]) == 'RB'
				adj_list.append(word)
			else:
				noun_list.append(word)
		else:
			noun_list.append(word)
	opinion_features_and_words.append(tuple((noun_list, adj_list)))

#for i in opinion_features_and_words:
	#print(i)

f = open('cleaned_data_for_pruning.txt', 'r')

sentences = []

for l in f:
	se = re.sub("[^\w\d'\s.]+",'',l)
	x = se.strip().split('.')
	for i in x:
		sentences.append(i.strip())
#print(sentences)

pos_words_in_sentence = []
words_in_sentence = []
for i in range(len(sentences)):
	if sentences[i] != '' or sentences[i] != '\n':
		sentence_word = sentences[i].split(' ')
		sentence_word = list(filter(None, sentence_word))
	if sentence_word != []:
		words_in_sentence.append(sentence_word)
		pos_words_in_sentence.append(pos_tag(sentence_word))

new_opinion_features_and_words = []
adj_opinion_word = []

for i in opinion_features_and_words:
	if i[1] == [] and i[0] != []:
		for ix, j in enumerate(words_in_sentence):
			adj_word = []
			flag = 0
			for w in i[0]:
				if w not in words_in_sentence[ix]:
					flag = 1
					break
			if flag == 0:
				index = words_in_sentence[ix].index(i[0][0])
				adj_ind = []
				for ind, pw in enumerate(pos_words_in_sentence[ix]):
					if pw[1] == 'JJ':
						adj_ind.append(ind)
				if adj_ind != []:
					mn = 1000
					min_ind = 1000
					for ind in adj_ind:
						if abs(ind - index) < mn:
							mn = abs(ind - index)
							min_ind = ind
					adj_word = [words_in_sentence[ix][min_ind]]
					#print(pos_tag(adj_word))
			if flag == 0 and adj_word != [] and pos_tag(adj_word)[0][1] == 'JJ':
				adj_opinion_word.append(adj_word[0])
				new_opinion_features_and_words.append(tuple((i[0], adj_word)))
	elif i[1] != [] and i[0] == []:
		for ix, j in enumerate(words_in_sentence):
			noun_word = []
			flag = 0
			for w in i[1]:
				if w not in words_in_sentence[ix]:
					flag = 1
					break
			if flag == 0:
				index = words_in_sentence[ix].index(i[1][0])
				noun_ind = []
				for ind, pw in enumerate(pos_words_in_sentence[ix]):
					if pw[1] == 'NN':
						noun_ind.append(ind)
				if noun_ind != []:
					mn = 1000
					min_ind = 1000
					for d in noun_ind:
						if abs(ind - index) < mn:
							mn = abs(ind - index)
							min_ind = ind
					noun_word = [words_in_sentence[ix][min_ind]]
			if flag == 0 and noun_word != [] and pos_tag(noun_word)[0][1] == 'NN':
				new_opinion_features_and_words.append(tuple((noun_word, i[1])))

opinion_words_list = []

for i in list(unique_everseen(new_opinion_features_and_words)):
	if i[0] != i[1]:
		opinion_words_list.append(i)
'''
for i in opinion_words_list:
	print(i)'''
count_simple = [0] * len(opinion_words_list)
for idx,i in enumerate(opinion_words_list):
	for sen in words_in_sentence:
		if set(i[0])<set(sen) and set(i[1])<set(sen):
			count_simple[idx] += 1

sortedlist_simple = [i[0] for i in sorted(zip(opinion_words_list, count_simple), key=lambda l: l[1], reverse=True)]
count2_simple = sorted(count_simple, reverse=True)

with open('opinion_words_list.pickle', 'wb') as handle:
    pickle.dump(sortedlist_simple, handle)

t = set(adj_opinion_word)
adj_opinion_word = []

for i in t:
	adj_opinion_word.append(i)

with open('adj_opinion_word.pickle', 'wb') as handle:
    pickle.dump(adj_opinion_word, handle)

### INFREQUENT FEATURE SETS ###
infrequent_feature_list = []

for i in opinion_features_and_words:
	if i[1] == [] and i[0] != []:
		for ix, j in enumerate(words_in_sentence):
			adj_word = []
			flag = 0
			for w in i[0]:
				if w not in words_in_sentence[ix]:
					flag = 1
					break
			if flag == 1:
				for adj in adj_opinion_word:
					if adj in words_in_sentence[ix]:
						index = words_in_sentence[ix].index(adj)
						noun_ind = []
						for ind, pw in enumerate(pos_words_in_sentence[ix]):
							if pw[1] == 'NN':
								noun_ind.append(ind)
						if noun_ind != []:
							mn = 1000
							min_ind = 1000
							for ind in noun_ind:
								if abs(ind - index) < mn:
									mn = abs(ind - index)
									min_ind = ind
							noun_word = [words_in_sentence[ix][min_ind]]
						if noun_word != [] and pos_tag(noun_word)[0][1] == 'NN':
							infrequent_feature_list.append(tuple((noun_word, [adj])))

new_infrequent_feature_list = []
for i in list(unique_everseen(infrequent_feature_list)):
	if i[0] != i[1]:
		new_infrequent_feature_list.append(i)

not_include = []
for ind, i in enumerate(new_infrequent_feature_list):
	if i in opinion_words_list:
		not_include.append(ind)

infrequent_feature_list = [i for j, i in enumerate(new_infrequent_feature_list) if j not in not_include]
'''
for i in infrequent_feature_list:
	print(i)
'''
all_feature_list = opinion_words_list + infrequent_feature_list

count = [0] * len(all_feature_list)
for idx,i in enumerate(all_feature_list):
	for sen in words_in_sentence:
		if set(i[0])<set(sen) and set(i[1])<set(sen):
			count[idx] += 1

sortedlist = [i[0] for i in sorted(zip(all_feature_list, count), key=lambda l: l[1], reverse=True)]
count2 = sorted(count, reverse=True)
for i in range(len(sortedlist)):
	print(sortedlist[i], count2[i])

with open('sorted_opinion_list.pickle', 'wb') as handle:
    pickle.dump(sortedlist, handle)