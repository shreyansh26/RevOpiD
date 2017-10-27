import re
import pickle

from nltk.stem import WordNetLemmatizer
from nltk.corpus import stopwords
from nltk import word_tokenize, pos_tag
import string, re
from autocorrect import spell
from nltk.tokenize import TweetTokenizer
from nltk.stem.lancaster import LancasterStemmer
from nltk.corpus import state_union
import nltk

lancaster_stemmer = LancasterStemmer()
tknzr = TweetTokenizer()  # This helps maintain apostrophe like "hasn't"
lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words("english"))

with open('words_tuple.pickle', 'rb') as handle:
    words_tuple = pickle.load(handle)

not_include = []
for i in range(len(words_tuple)-1):
	for j in range(i+1, len(words_tuple)):
		if set(words_tuple[i][0]) >= set(words_tuple[j][0]) and set(words_tuple[j][0]) >= set(words_tuple[i][0]):
			not_include.append(j)

words_tuple = [i for j, i in enumerate(words_tuple) if j not in not_include]


PROD = 'AutomotiveProd3'
f = 'text_0_'+PROD+'.txt'
#f = '0_'+PROD+'.txt'
f = open(f)

RE_D = re.compile('\d')
RE_AP = re.compile("'")
RE_DO = re.compile("\.")

limit = 0
i = 0
r = []
for l in f:
	i+=1
	if(i==limit):
		break
	if(i%2==0):
		continue
	r.append(l[:-1])
f.close()

for idx,r1 in enumerate(r):
	r1 = r1.lower()
	r[idx] = tknzr.tokenize(r1)   # or word_tokenize(r1)
	for i in range(len(r[idx])):
		if ((r[idx][i] not in set(string.punctuation)) and (not (RE_D.search(r[idx][i]))) and (not (RE_AP.search(r[idx][i]))) and (not (RE_DO.search(r[idx][i]))) and (r[idx][i] != 'was') and (r[idx][i] != 'has')):
			r[idx][i] = spell(r[idx][i])
		if r[idx][i] in stop_words:
			r[idx][i] = ''
		r[idx][i] = lemmatizer.lemmatize(r[idx][i])  # or lancaster_stemmer.stem(r[idx][i]) but changes the word a lot

	r[idx] = (' ').join(r[idx])
	r[idx] = r[idx].strip()
	r[idx] = r[idx].lower()
	r[idx] = re.sub('\s+', ' ', r[idx])
	#r[idx] = r[idx].translate(str.maketrans('','',string.punctuation))

#print(r[0].split())
f = open('cleaned_data_for_pruning.txt', 'w')
for i in r:
	f.write(i)
	f.write('\n')

f.close()


f = open('cleaned_data_for_pruning.txt', 'r')

sentences = []

for l in f:
	se = re.sub("[^\w\d'\s.]+",'',l)
	x = se.strip().split('.')
	for i in x:
		sentences.append(i.strip())
#print(sentences)

words_in_sentence = []
for i in range(len(sentences)):
	if sentences[i] != '' or sentences[i] != '\n':
		sentence_word = sentences[i].split(' ')
	words_in_sentence.append(sentence_word)

#print(words_in_sentence)

not_prune = []
for k in range(len(words_tuple)):
	indices = []
	if len(words_tuple[k][0]) >= 2:
		flag2 = 0
		count = 0
		for word_list in words_in_sentence:
			if set(word_list).issuperset(set(words_tuple[k][0])):
				for l, wo in enumerate(words_tuple[k][0]):
					indices.append(word_list.index(words_tuple[k][0][l]))
					indice_diff = [abs(i-j) for i in indices for j in indices if i != j]
					flag = 0
					for diff in indice_diff:
						if diff > 3:
							flag = 1
							break
					if flag == 0:
						count += 1
		if count < 2:
			not_prune.append(k)

#print(not_prune)

compactness_pruned_feature_list = [i for j, i in enumerate(words_tuple) if j not in not_prune]

for ind, i in enumerate(compactness_pruned_feature_list):
	count = 0
	for sent in words_in_sentence:
		if set(i[0]) < set(sent):
			count += 1
	compactness_pruned_feature_list[ind] = list(i)
	compactness_pruned_feature_list[ind][1] = count
	compactness_pruned_feature_list[ind] = tuple(compactness_pruned_feature_list[ind])
	
with open('compactness_pruned_feature_list.pickle', 'wb') as handle:
    pickle.dump(compactness_pruned_feature_list, handle)
'''
for i in compactness_pruned_feature_list:
	if i[0] != []:
		print(i)

'''