import re
import pickle
from nltk.corpus import wordnet as wn

with open('sorted_opinion_list.pickle', 'rb') as handle:
    sorted_opinion_list = pickle.load(handle)

#with open('positive_adjectives.pickle', 'rb') as handle:
    #positive_adjectives = pickle.load(handle)

#with open('negative_adjectives.pickle', 'rb') as handle:
    #negative_adjectives = pickle.load(handle)

seed_list = [('great', 1), ('fantastic', 1), ('nice', 1), ('cool', 1), ('awesome', 1), ('bad', -1), ('dull', -1), ('big', 1), ('small', -1), ('good', 1), ('strong', 1), ('new', 1), ('old', -1), ('light', 1), ('heavy', -1), ('many', 1), ('huge', 1), ('high', 1), ('low', 0), ('hard', -1), ('soft', 1), ('large', 1), ('tiny', -1), ('effective', 1), ('satisfied', 1), ('outstanding', 1), ('fast', 1), ('slow', -1), ('pleased', 1), ('happy', 1), ('sad', -1)]
#seed_list.extend(positive_adjectives)
#seed_list.extend(negative_adjectives)

not_include = []

with open('opinion_words_list.pickle', 'rb') as handle:
    opinion_words_list = pickle.load(handle)

count_in = []

def OrientationPrediction(opinion_words_list, seed_list):
	count = 0
	while True:
		size1 = len(seed_list)
		OrientationSearch(opinion_words_list, seed_list)
		size2 = len(seed_list)
		if size1 == size2:
			notOrientationSearch(opinion_words_list, seed_list)
			break
		elif count >= 100:
			notOrientationSearch(opinion_words_list, seed_list)
			break

def OrientationSearch(opinion_words_list, seed_list):
	for word_pair in opinion_words_list:
		if ([item for item in seed_list if item[0] == word_pair[1][0]] == []) and (word_pair not in not_include):
			word_synset = wn.synsets(word_pair[1][0], pos='a')
			if word_synset == []:
				not_include.append(word_pair)
				continue
			else:
				antonym_list = []
				synonym_list = []
				for syn in word_synset:
					for l in syn.lemmas():
						synonym_list.append(l.name())
						if l.antonyms():
							antonym_list.append(l.antonyms()[0].name())
				flag = 0
				for synonym in synonym_list:
					if ([item for item in seed_list if item[0] == synonym] != []):
						a = [item for item in seed_list if item[0] == synonym][0]
						seed_list.append((word_pair[1][0], a[1]))
						flag = 1
						count_in.append(1)
						break
				for antonym in antonym_list:
					if ([item for item in seed_list if item[0] == antonym] != []):
						a = [item for item in seed_list if item[0] == antonym][0]
						seed_list.append((word_pair[1][0], -a[1]))
						flag = 1
						count_in.append(1)
						break


def notOrientationSearch(opinion_words_list, seed_list):
	for word_pair in opinion_words_list:
		if [item for item in seed_list if item[0] == word_pair[1][0]] == [] and word_pair not in not_include:
			not_include.append(word_pair)


OrientationPrediction(opinion_words_list, seed_list)
'''for i in set(seed_list):
	print(i)

print('\n\n')

for i in not_include:
	print(i)

print('\n\n')

print(count_in)'''

for ind, word_pair in enumerate(opinion_words_list):
	if ([item for item in seed_list if item[0] == word_pair[1][0]] != []):
		a = [item for item in seed_list if item[0] == word_pair[1][0]]
		opinion_words_list[ind] = [word_pair, a[0][1]]
	elif word_pair in not_include:
		opinion_words_list[ind] = [word_pair, 0]
'''
for i in opinion_words_list:
	print(i)'''

positive_opinion_list = []
negative_opinion_list = []
neutral_opinion_list = []

for i in opinion_words_list:
	if i[1] == 1:
		positive_opinion_list.append(i)
	elif i[1] == -1:
		negative_opinion_list.append(i)
	else:
		neutral_opinion_list.append(i)


positive_noun_set = []
negative_noun_set = []
neutral_noun_set = []

for i in positive_opinion_list:
	positive_noun_set.append(i[0][0][0])
positive_noun_set = set(positive_noun_set)

for i in negative_opinion_list:
	negative_noun_set.append(i[0][0][0])
negative_noun_set = set(negative_noun_set)

for i in neutral_opinion_list:
	neutral_noun_set.append(i[0][0][0])
neutral_noun_set = set(neutral_noun_set)

positive_opinion_list =  sorted(positive_opinion_list)
negative_opinion_list = sorted(negative_opinion_list)
neutral_opinion_list = sorted(neutral_opinion_list)

positive_dict = {}
negative_dict = {}
neutral_dict = {}

for i in positive_noun_set:
	positive_dict[i] = []
	for j in positive_opinion_list:
		if j[0][0][0] == i:
			positive_dict[i].append(j)

for i in negative_noun_set:
	negative_dict[i] = []
	for j in negative_opinion_list:
		if j[0][0][0] == i:
			negative_dict[i].append(j)

for i in neutral_noun_set:
	neutral_dict[i] = []
	for j in neutral_opinion_list:
		if j[0][0][0] == i:
			neutral_dict[i].append(j)
'''
for x in positive_dict:
    print(x)
    print(positive_dict[x])'''

with open('positive_dict.pickle', 'wb') as handle:
    pickle.dump(positive_dict, handle)

with open('negative_dict.pickle', 'wb') as handle:
    pickle.dump(negative_dict, handle)

with open('neutral_dict.pickle', 'wb') as handle:
    pickle.dump(neutral_dict, handle)
'''
for i in seed_list:
	print(i)'''
