from random import randint
from sklearn.cluster import KMeans
import matplotlib

import numpy as np
import torch

#GLOVE_PATH = '../dataset/GloVe/glove.840B.300d.txt'
#GLOVE_PATH = 'glove.twitter.27B.200d.txt'
GLOVE_PATH = '../dataset/GloVe/glove.6B.300d.txt'

model = torch.load('infersent.allnli.pickle', map_location=lambda storage, loc: storage)
#print model
#print type(model)
model.set_glove_path(GLOVE_PATH)

model.build_vocab_k_words(K=100000)

"""sentences = []
with open('text_0_AutomotiveProd1.txt') as f:
    for line in f:
	l1 = line.split()
	l3_list = []
	for i,l2 in enumerate(l1):
		l3 = l2.lower()
		l3_list.append(l3)
        sentences.append(l3_list.join(' ').strip())
print(len(sentences))"""

sentences = []
with open('text_0_AutomotiveProd1.txt') as f:
    for line in f:
        sentences.append(line.strip())
#print(len(sentences))

#e = model.encode(['the cat eats'])
#print e

#exit()

#print sentences
textVect = []
for s in sentences:
	e = model.encode([s])
	textVect.append(e)

## K-means ##
num_clusters = 10
km = KMeans(n_clusters=num_clusters)
km.fit(textVect)
clusters = km.labels_.tolist()

## Print Sentence Clusters ##
cluster_info = {'sentence': sentences, 'cluster' : clusters}
sentenceDF = pd.DataFrame(cluster_info, index=[clusters], columns = ['sentence','cluster'])

for num in range(num_clusters):
	print "Sentence cluster: ", int(num+1),
	print()
	#print(sentenceDF.ix[num]['sentence'])
	for sentence in sentenceDF.ix[num]['sentence']:
		print sentence,
		print ''
	print ''

exit()
embeddings = model.encode(sentences, bsize=128, tokenize=False, verbose=True)
print('nb sentences encoded : {0}'.format(len(embeddings)))

for e in embeddings:
	print e

np.linalg.norm(model.encode(['the cat eats.']))

def cosine(u, v):
    return np.dot(u, v) / (np.linalg.norm(u) * np.linalg.norm(v))

cosine(model.encode(['the cat eats.'])[0], model.encode(['the cat drinks.'])[0])

idx = randint(0, len(sentences))
_, _ = model.visualize(sentences[idx])

my_sent = 'The cat is drinking milk.'
_, _ = model.visualize(my_sent)
