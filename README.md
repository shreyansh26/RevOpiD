Review Opinion Diversification
==============================

A project I worked on in my 3rd Semester. RevOpiD was a shared task in IJCNLP 2017, Taipei, Taiwan. [Task](https://sites.google.com/itbhu.ac.in/revopid-2017)

I implemented three approaches of solving the problem -  
1. Opinion Feature Mining (**Mining Opinion Features in Customer Reviews** (Liu et al.))
2. Doc2Vec Model and Clustering
3. Facebook Research's InferSent model for sebtence embeddings with CLustering [Link](https://github.com/facebookresearch/InferSent)

Three clustering techniques were tried -
* K-Means Clustering
* Spectral Clustering
* Agglomerative Clustering

Of the three, the Spectral Clustering gave the best results on manually checking the contents of the clusters. So in all the description of the models, clustering implies Spectral Clustering.
