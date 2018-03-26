Review Opinion Diversification
==============================

A project I am currently working on in my 3rd Sem. I am implementing the paper **Mining Opinion Features in Customer Reviews** (Liu et al.) with some improvements which I will add later.

Requirements
------------

* Python 3.5  
* NLTK  
* Python package **autocorrect**  
  `pip3 install autocorrect`
* Python package **more_itertools**  
  `pip3 install more_itertools`

To run
------

Should be run preferably on a Unix/Liux distro.  

The name of the dataset file is required in **mining_opinion_features.py**, **compactness_pruning.py** and **select_nouns.py**   

Simply go to the root of the project and type in the terminal - `make`  

The final list of opinion words are in the list **output_list.txt**  

The opinion matrices are also produced in csv format.
