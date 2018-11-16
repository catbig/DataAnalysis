# DataAnalysis
* Author : Catur Adi Nugroho
* Time   : 20160327 00:20
* Content: Data Analysis

Feature
1. Read file using univocity-parsers
2. Sorting using google-externalsorting
3. Find and matching string using lucene
4. Threshold for LetterPairSimilarity algorithm
5. Multi thread processing

Issue:
1. Big Data
2. Messy Data

Algorithm:
1. Read file (use univocity-parsers)
2. Clean name
3. Split name
4. Save to cleaned file
5. Sorting file (use external sort algorithm)
6. Compute file (use LetterPairSimilarity algorithm)

Program:
Program Information:
1. Run run.bat
2. Open file
   Open file location
3. Clean up
   Clean up and sorting data, generate file clean data.txt & sort data.txt
4. Process
   a. Threshold
      You can set threshold before you process the data.
   b. Matching string use Letter Pair Similarity algorithm
   c. Generate file result.txt
5. Clear
   clear log
6. Exit

Project Information:
1. DataAnalysis Maven Project build by NetBeans
2. Compile and export with JDK 1.7.x
