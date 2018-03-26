from nltk.stem import WordNetLemmatizer
from nltk.corpus import stopwords
from nltk import word_tokenize, pos_tag, sent_tokenize, RegexpTokenizer
import string, re
from autocorrect import spell
from nltk.tokenize import TweetTokenizer
from nltk.stem.lancaster import LancasterStemmer
from nltk.corpus import state_union
import nltk
import gensim
import pandas as pd
from sklearn.cluster import KMeans
import os
import pickle

lancaster_stemmer = LancasterStemmer()
tknzr = TweetTokenizer()  # This helps maintain apostrophe like "hasn't"
lemmatizer = WordNetLemmatizer()
stop_words = set(stopwords.words("english"))
tokenizer = RegexpTokenizer(r'\w+')

RE_D = re.compile('\d')
RE_AP = re.compile("'")
RE_DO = re.compile("\.")

path = './TEXT_REVIEWS'
dirs = os.listdir(path)

db = []
docLabel = []
dset = 0

for File in dirs:
	f = open(path+'/'+File,'r')
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
		r[idx] = re.sub(r'\.([a-zA-Z0-9])', r'. \1', r[idx])
	for ind, item in enumerate(r):
		new_list = [a for a in tknzr.tokenize(item) if a not in string.punctuation]
		db.append(new_list)
		docLabel.append(str(File)[:-4]+'_'+str(ind))
	dset += 1

print(len(db))
print(len(docLabel))
#print(db[1000])
with open('db.pickle', 'wb') as handle:
    pickle.dump(db, handle, protocol=2)
with open('docLabel.pickle', 'wb') as handle:
    pickle.dump(docLabel, handle, protocol=2)
