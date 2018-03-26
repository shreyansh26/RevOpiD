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

with open('db.pickle', 'rb') as handle:
	db = pickle.load(handle)

with open('docLabel.pickle', 'rb') as handle:
	docLabel = pickle.load(handle)

#loading the model
d2v_dbow_model = gensim.models.doc2vec.Doc2Vec.load('complete_doc2vec_dbow.model')
d2v_dmc_model = gensim.models.doc2vec.Doc2Vec.load('complete_doc2vec_dmc.model')

def print_review_similarity(d2v_model):
	sims = d2v_model.docvecs.most_similar('text_0_AutomotiveProd7_23') #text_0_AutomotiveProd7_1
	#print(sims)
	print()
	print(' '.join(db[docLabel.index('text_0_AutomotiveProd7_23')]))
	for i in sims:
		print(i, ' '.join(db[docLabel.index(i[0])]))
	print()
	sims2 = d2v_model.most_similar('good') #text_0_AutomotiveProd7_1
	print(sims2)
	sims3 = d2v_model.most_similar('tasty') #text_0_AutomotiveProd7_1
	print(sims3)
	sims4 = d2v_model.most_similar('love') #text_0_AutomotiveProd7_1
	print(sims4)
	sims5 = d2v_model.most_similar('cold') #text_0_AutomotiveProd7_1
	print(sims5)

#print_review_similarity(d2v_dbow_model)
#print_review_similarity(d2v_dmc_model)
'''sims2 = d2v_model.most_similar('good') #text_0_AutomotiveProd7_1
print(sims2)
sims2 = d2v_model.most_similar('tasty') #text_0_AutomotiveProd7_1
print(sims2)
sims2 = d2v_model.most_similar('love') #text_0_AutomotiveProd7_1
print(sims2)
sims2 = d2v_model.most_similar('cold') #text_0_AutomotiveProd7_1
print(sims2)'''
#print(d2v_model.docvecs['text_0_AutomotiveProd7_1'])

reqdlabels = [] 
reqddocs = []
for ind, item in enumerate(docLabel):
	if item.startswith('text_0_OfficeProd18'):
		reqdlabels.append(item)
		reqddocs.append(db[ind])

textVect = []
ind = 0
for i in reqdlabels:
	textVect.append(d2v_dbow_model.docvecs[i])

## K-means ##
num_clusters = 15
km = KMeans(n_clusters=num_clusters)
km.fit(textVect)
clusters = km.labels_.tolist()

## Print Sentence Clusters ##
cluster_info = {'sentence': reqddocs, 'cluster' : clusters}
sentenceDF = pd.DataFrame(cluster_info, index=[clusters], columns = ['sentence','cluster'])

reqddocsNew = []
reqdlabelsNew = []
textVectNew = []
for num in range(num_clusters):
	#print("Sentence cluster %d: " %int(num+1), end='')
	#print()
	#print(sentenceDF.ix[num]['sentence'])
	flag = 0
	i = 1
	for sentence in sentenceDF.ix[num]['sentence']:
		if isinstance(sentence, list):
			#print(str(i)+'. ', end='')
			#i += 1
			#print(' '.join(sentence))
			textVectNew.append(d2v_dbow_model.docvecs[reqdlabels[reqddocs.index(sentence)]])
			reqddocsNew.append(sentence)
			reqdlabelsNew.append(reqdlabels[reqddocs.index(sentence)])
		else:
			flag = 1
			break
	#if flag == 1:
		#print('1. ', end='')
		#print(' '.join(sentenceDF.ix[num]['sentence']))
		#print()
	#print()

## Re-apply K-means ##
num_clusters = 15
km = KMeans(n_clusters=num_clusters)
km.fit(textVectNew)
clusters = km.labels_.tolist()

## Print Sentence Clusters ##
cluster_info = {'sentence': reqddocsNew, 'cluster' : clusters}
sentenceDF = pd.DataFrame(cluster_info, index=[clusters], columns = ['sentence','cluster'])

for num in range(num_clusters):
	print("Sentence cluster %d: " %int(num+1), end='')
	print()
	#print(sentenceDF.ix[num]['sentence'])
	flag = 0
	i = 1
	for sentence in sentenceDF.ix[num]['sentence']:
		if isinstance(sentence, list):
			print(str(i)+'. ', end='')
			i += 1
			print(' '.join(sentence))
		else:
			flag = 1
			break
	if flag == 1:
		print('1. ', end='')
		print(' '.join(sentenceDF.ix[num]['sentence']))
		print()
	print()