# Google-PageRank
Realized PageRank by processing data by MapReduce, visualized by JS

## Algorithm: PageRank
is an algorithm used by Google search to rank websites in their search engine results
Two Important Assumptions: 
- Number Assumption: More important websites are likely to receive more links from other websites
- Weight Assumption: Websites with higher PageRank will pass higher weight.\

First use old method like “key word”/ “click rate” to select the pages that are relevant to the target. And then user “PageRank” to do sorting job.

### Number Assumption: More important websites are likely to receive more links from other websites

How to represent the trasmission between different pages:
Use transition matrix (This is a term in this algorithm)
Column represents “From”, Row represents “To”
Probability: If A linked with B, C, D, then the possiblity of A would jump to B, C, D website are all 1/3. (Since we don’t know the exact possiblity, so we simplfy the problem to make it tobe Equal Probability.
- Example: A -> BCD, B -> AD, C -> A, D -> B,C 
>       Transition matrix would be: 
              To/From      A     B    C    D 
                 A         0    1/2   1    0 
                 B        1/3    0    0   1/2 
                 C        1/3    0    0   1/2 
                 D        1/3   1/2   0    0 
 
### Weight Assumption: Websites with higher PageRank will pass higher weight

How to reqresents page’s weight:
Before generating matrix, all the pages are equally weighted.
The weight can be calculated by using initial weight vector multiply transition matrix.
- For example:
    Initial vector: [1/4, 1/4, 1/4, 1/4]
- weight can be calculated by: 
>            PR1 = Transition Matrix * PR0 :
                         0    1/2   1    0                   1/4             9/24              -> A
                        1/3    0     0   1/2      *          1/4       =     5/24              -> B
                        1/3    0     0   1/2                 1/4             5/24              -> C
                        1/3   1/2    0    0                  1/4             5/24              -> D
- PR2 = Transition Matrix * PR1
- PRN = Transition Matrix * PRN-1
#### Do N recursion!
After the recursion, the important pages would be more and more important, and the less important pages would be less and less important.

### Edge Cases:

1. Dead End: Websites only be pointed by other websites, but they do not point any other websites. This kind of webpage only “eat” weights from other websites, but never share its own weight to other websites. -> The total weight will be eaten and all the values would close to zero.
2. Spider traps: Websites that only pointed to themselves - > PRN Matrix will be dominated by this page. (All the values except this page would be zero and this page would be 1)
### How to solve edge cases:

By introducing a “teleporting” variable!
- Before:  PR(N) =  Transition Matrix * PR(N-1)
- Now:     PR(N) = (1-ß)Transition Matrix * PR(N-1) + ß * PR(N-1)
First we try to think about when a human being enter a dead-end or spider-traps website, what would he do to break this endless loop? -> He would simply close this website and reach to another website!
ß is the possibability that a human would stop watching and close this website. According to the existing research paper, the value of ß is around 0.15 - 0.2.

### Fit the task into MapReduce Framework

1. Can we directly do Transition Matrix * PR?   No, this would cost too much memory, we need to split the Matrix into small section. More importantly, if we do matrix * matrix, only one reducer can be used and this reducer would need to store these two big matrix. If we separate the matrix, we can use multiple reducers to do this work. Also, some weights equal to zero, which is useless and we don’t need to store it into memory (Waste Memory). We should simply skip them. So matrix would never be the best data structure as input in MapReduce
2. Split the Matrix to rows or directly split the matrix into numbers. Split matrix into numbers, since rows actually would introduce more work. Assume that there is a new website introduced into the dataset, we need to iterate the whole dataset to change the whole matrix. - > Not easy to manipulate 
3. If we split the matrix into single number, when introducing new webpages, we only need to append new data into the original one. We don’t need to manipulate other columns/rows
4. Considering the MapReduce structure(key-value), we give each website an id to identify and determine which cell should multiply by which cell.

### PageRank Workflow
- Generate Transition Matrix Cell -> Generate PR Matrix Cell -> Transition Matrix Cell * PR Matrix Cell = Sum Up cell for Each Page 

- Raw data format (should be as clean as possible):
Single direction, id to id:
1 2 (from page1 to page2)
2 4 (from page2 to page4)

### Reference: Data Origin
https://www.limfinity.com/ir/

### Reference: Develop Environment
- Java SE8
- Hadoop 2.7.2
- Python 3.7

### Reference: Picture Library
- Qiantu Website: http://www.58pic.com/ (copyright: Commercially available)
