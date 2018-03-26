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
from gensim.test.test_doc2vec import ConcatenatedDoc2Vec
import pandas as pd
from sklearn.cluster import KMeans
import os
import pickle
from random import shuffle

with open('db.pickle', 'rb') as handle:
	db = pickle.load(handle)

with open('docLabel.pickle', 'rb') as handle:
	docLabel = pickle.load(handle)


class LabeledLineSentence(object):
    def __init__(self, doc_list, labels_list):
        self.labels_list = labels_list
        self.doc_list = doc_list

    def __iter__(self):
        for idx, doc in enumerate(self.doc_list):
              yield gensim.models.doc2vec.LabeledSentence(doc, [self.labels_list[idx]])

data = db

#iterator returned over all documents
it = LabeledLineSentence(data, docLabel)

model1 = gensim.models.Doc2Vec(dm=0, size=300, min_count=10, alpha=0.025, min_alpha=0.001, dbow_words=1) # DBOW
model2 = gensim.models.Doc2Vec(dm=1, size=300, min_count=10, alpha=0.025, min_alpha=0.001, dm_concat=1) # DBOW


## Only DBOW
model.build_vocab(it)
total_epoch = 100
#training of model
model.train(it, total_examples=len(data), epochs=total_epoch)

#saving the created model
model.save('complete_doc2vec_dbow.model')
print("model_dbow saved")

## Only DMC
model2.build_vocab(it)
total_epoch = 100
#training of model
model2.train(it, total_examples=len(data), epochs=total_epoch)

#saving the created model
model2.save('complete_doc2vec_dmc.model')
print("model_dmc saved")

'''
## DBOW + DMC
model3 = ConcatenatedDoc2Vec([model1, model2])
total_epoch = 100
#training of model
model3.train(it, total_examples=len(data), epochs=total_epoch)

#saving the created model
model3.save('complete_doc2vec_dbow+dmc.model')
print("model_dbow+dmc saved")
'''