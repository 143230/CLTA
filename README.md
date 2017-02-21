#CLTA
This is a project that tries to align cross-lingual taxonomies on the Web, such as product catalogues and Web site directories. 
Here, we publish all the source code.

###Requirements:

1. JDK 1.8.0_111
2. Maven 3.3.9

###Data you need:
1. Biterm Documents or Word Documents
2. Biterm-Category or Document-Category Distribution file

###Biterm Documents content format:
each line represents a category biterm document organised as follows:<br/>
```<category url>@#@#@<category label>@#@#@<category lang>@#@#@<chinese-chinese biterm document>@#@#@<chinese-english biterm document>@#@#@<english-english biterm document>```<br/>
for example：<br/>
```http://www.ebay.com/chp/Fins-/16054@#@#@Fins@#@#@en@#@#@[呼吸	手套,...]@#@#@[呼吸	full,...]@#@#@[cheap	sailor,...]```<br/>
###Word Documents content format:
each line represents a category word document organised as follows:<br/>
```<category url>@#@#@<category label>@#@#@<category lang>@#@#@<chinese word document>@#@#@<translated english word document>```<br/>
for example：<br/>
```http://conference_en#c-7081035-6117083@#@#@committee@#@#@en@#@#@[任命, 报告...]@#@#@[elect, person...]```<br/>
###Biterm-Category Distribution file content format:
each line represents a biterm-category distribution organised as follows:<br/>
```<word1>@#@#@<word2>@#@#@<lang1_lang2>\t[<category url>@#@#@<category distribution>,...]```<br/>
for example:<br/>
```稿件@#@#@carry@#@#@ZH_EN        [http://cmt_cn#c-8430559-8614325@#@#@1.0]```<br/>

###Document-Category Distribution file content format:
each line represents a document-category distribution organised as follows:<br/>
```<document id>@#@#@<document label>@#@#@<document language>\t[<category url>@#@#@<category distribution>, ...]```<br/>
for example:<br/>
```http://cmt_cn#c-1609047-4017692@#@#@合著者@#@#@zh@#@#@  [http://cmt_cn#c-1609047-4017692@#@#@1.0]```<br/>

###input file organization:
suppose the dataset name is 'A', for CC-BiLDA method, the Word Documents and the Document-Category Distribution file are as:<br/>
```corpus/A/exact matching/CC-BiLDA/TextPairs(for BiLDA)_A```<br/>
```corpus/A/exact matching/CC-BiLDA/TextPairs(for BiLDA)_A.(<avg_pi> or <hier_pi>)```<br/>
for CC-BiBTM method, the Biterm Documents and the Biterm-Category Distribution file are as:<br/>
```corpus/A/exact matching/CC-BiBTM/Biterms(for BiBTM)_A```<br/>
```corpus/A/exact matching/CC-BiBTM/Biterms(for BiBTM)_A.(<avg_pi> or <hier_pi>)```<br/>

###Compile Project:
To run this project, you need to first compile this project using maven:<br/>
```mvn assembly:assembly```<br/>

#Run Project:
Then the jar package of this project will be generated in the target directory named by 'alignment-1.0-SNAPSHOT.jar'<br/>

if you are first time to using this project, run:<br/>
```java -jar target\alignment-1.0-SNAPSHOT.jar -h```<br/>
you will get the help options<br/>
```
usage: Model Run Options
 -alpha <arg>         Hyper Parameter Alpha
 -avg                 Using Average Category Distribution to inference the
                      GibbsSampling.
 -f <arg>             File Name
 -h                   HELP_DESCRIPTION
 -hier                Using Hierarchy Category Distribution to inference
                      the GibbsSampling.
 -iter <arg>          Iteration Number
 -k <arg>             Topic Number
 -m <arg>             Method for training the corpus, one of <CCBiBTM,
                      CCBiLDA>
 -minus <arg>         Corpus Minus
 -savestep <arg>      Step to Save
 -source_beta <arg>   Source Beta
 -t <arg>             Data Type
 -target_beta <arg>   Target Beta```<br/>


then you can following the help option to run this project on your own datasets. for example, you can run:<br/>
```java -jar target/alignment-1.0-SNAPSHOT.jar -m CCBiBTM -f "Biterms(for BiBTM)" -t "product catalogue" -iter 300 -savestep 100 -minus "_minus(0.333)" -k 100```<br/>
if options not refered, values will be put default.
