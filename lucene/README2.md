### Lucene索引库查询

对要搜索的信息创建Query查询对象，Lucene会根据Query查询对象生成最终的查询语法，类似关系数据库Sql语法一样Lucene也有自己的查询语法，
比如：“name:lucene”表示查询Field的name为“lucene”的文档信息

* 可通过两种方法创建查询对象
    
    * 使用Lucene提供Query子类
    
        Query是一个抽象类，lucene提供了很多查询对象，比如TermQuery项精确查询，NumericRangeQuery数字范围查询等。
        
        如下代码：
        
        ```
        Query query = new TermQuery(new Term("name", "lucene"));
        ```
    * 使用QueryParse解析查询表达式
    
        QueryParse会将用户输入的查询表达式解析成Query对象实例。
        
        如下代码: 
        
        ```
        QueryParser queryParser = new QueryParser("name", new IKAnalyzer());
        		Query query = queryParser.parse("name:lucene");
        ```
      
 #### 使用query的子类查询  
 
 * MatchAllDocsQuery
 
    * 使用MatchAllDocsQuery查询索引目录中的所有文档
    
    * 实例代码(工具方法下面的所有方法都会用到这个两个抽出来的工具方法)
    
    ```
    //工具方法1
    public IndexSearcher getIndexSearcher() throws IOException {
            //1, 指定索引库存储的位置
            Directory directory = FSDirectory.open(new File("/tmp/index/"));
            //2,使用IndexReade 对象打开索引库
            IndexReader indexReader = DirectoryReader.open(directory);
            //3,创建一个IndexSearcher 对象 ,构造方法中需要 IndexReade 参数
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            return indexSearcher;
        }
    //工具方法2    
    public void printResult(IndexSearcher indexSearcher, Query query) throws IOException {
            //查询索引库
            TopDocs topDocs = indexSearcher.search(query, 100);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            System.out.println("查询总记录数" + topDocs.totalHits);
            //遍历查询结果
            for (ScoreDoc scoreDoc : scoreDocs) {
                int docId = scoreDoc.doc;
                //通过id查询文档对象
                Document doc = indexSearcher.doc(docId);
                //取属性
                System.out.println(doc.get("name"));
                System.out.println(doc.get("size"));
                System.out.println(doc.get("content"));
                System.out.println(doc.get("path"));
            }
            indexSearcher.getIndexReader().close();
        }    
    ```
    * 使用MatchAllDocsQuery查询代码实例
    
    ```
        /**
         * 使用query 的子类进行查询
         * MatchAllDocsQuery
         */
        @Test
        public void testMatchAllDocsQuery() throws IOException {
            IndexSearcher indexSearcher = getIndexSearcher();
            //创建查询条件
            Query query = new MatchAllDocsQuery();
            //执行查询
            printResult(indexSearcher, query);
        }
    ```
    
 * TermQuery
 
    * TermQuery，通过项查询，TermQuery不使用分析器所以建议匹配不分词的Field域查询，比如订单号、分类ID号等。
      指定要查询的域和要查询的关键词   
      
    * 实例代码
    
    ```
        /**
         * 使用TermQuery 查询
         * TermQuery 通过项查询,TermQuery不使用分析器所以建议匹配不分词的Field 域查询
         * 比如订单号,分类 ID 等,
         * 指定要查询的域和要查询的关键词。
         * 查询内容中含有 lucene
         */
        @Test
        public void testTermQuery() throws IOException {
            IndexSearcher indexSearcher = getIndexSearcher();
            //创建查询对象
            Query query = new TermQuery(new Term("content", "lucene"));
            //执行查询
            printResult(indexSearcher,query);
        }
        
    ```
  * NumericRangeQuery
  
    * 可以根据数值范围查询。
    
    * 实例代码
    
    ```
        /**
         * 根据数据范围进行查询  NumericRangeQuery
         *
         * @throws IOException
         */
        @Test
        public void testNumericRangeQuery() throws IOException {
            IndexSearcher indexSearcher = getIndexSearcher();
            //创建查询
            /*
             * 参数1:域名
             * 参数2:最小值
             * 参数3:最大值
             * 参数4:是否包含最小值
             * 参数5:是否包含最大值
             */
            Query query = NumericRangeQuery.newLongRange("size", 1L, 1000L, true, true);
            printResult(indexSearcher, query);
        }
    ```  
  * BooleanQuery

       * 可以组合查询条件。
       
       * 实例代码
       
       ```
            /**
            * 组合条件查询 BooleanQuery
            *
            * @throws IOException Occur.MUST：必须满足此条件，相当于and   : +content:apache
            *                     Occur.SHOULD：应该满足，但是不满足也可以，相当于or  : content:apache
            *                     Occur.MUST_NOT：必须不满足。相当于not  -content:apache
            */
           @Test
           public void testBooleanQuery() throws IOException {
               IndexSearcher indexSearcher = getIndexSearcher();
               BooleanQuery booleanClauses = new BooleanQuery();
               //Query query = NumericRangeQuery.newLongRange("size", 1L, 1000L, true, true);
               Query query = new TermQuery(new Term("content", "apache"));
               Query query1 = new TermQuery(new Term("name", "apache"));
               booleanClauses.add(query, BooleanClause.Occur.MUST);//必须满足
               booleanClauses.add(query1, BooleanClause.Occur.MUST);
               System.out.println(booleanClauses);//  +content:apache +name:apache
               //执行查询
               printResult(indexSearcher, booleanClauses);
           }

       ``` 
  #### 使用queryparser查询  
  
  **所需依赖**     
  
  ```
           <dependency>
              <groupId>org.apache.lucene</groupId>
              <artifactId>lucene-queryparser</artifactId>
              <version>4.10.4</version>
          </dependency>
  ```
  * 代码实例
  
  ```
       /**
       * 通过QueryParser也可以创建Query，QueryParser提供一个Parse方法，此方法可以直接根据查询语法来查询。
       * Query对象执行的查询语法可通过System.out.println(query);查询。
       * 需要使用到分析器。建议创建索引时使用的分析器和查询索引时使用的分析器要一致。
       *
       * @throws IOException
       */
      @Test
      public void testQueryParser() throws IOException, ParseException {
          IndexSearcher indexSearcher = getIndexSearcher();
          //第一个参数是搜索的域 , 第二个参数指定分析器对象(建议和创建索引时使用的分析器和查询索引时使用的分析器要一致。)
          QueryParser queryParser = new QueryParser("content", new StandardAnalyzer()); //new IKAnalyzer()
          /**
           * 下面这几种注释的可以打开运行一下看看效果
           */
          //Query query = queryParser.parse("mybatis is apache project");
          //Query query=queryParser.parse("name:spring"); //指定特定的域和内容
          //Query query=queryParser.parse("*:*"); //会将全部查询出来
          //Query query=queryParser.parse("size:{100 TO 1000]");lucene不支持此数值类型的语法,但solr在基础上进行了扩展 solr是支持的
          Query query = queryParser.parse("+content:apache +name:apache");
          printResult(indexSearcher, query);
      }
  ```
 ```
    查询语法
    1、基础的查询语法，关键词查询：
    域名+“：”+搜索的关键字
    例如：content:java
    范围查询
    域名+“:”+[最小值 TO 最大值]
    例如：size:[1 TO 1000]
    范围查询在lucene中不支持数值类型，支持字符串类型。在solr中支持数值类型。
    组合条件查询
    1）+条件1 +条件2：两个条件之间是并且的关系and
    例如：+filename:apache +content:apache
    +条件1 条件2：必须满足第一个条件，应该满足第二个条件
    例如：+filename:apache content:apache
    条件1 条件2：两个条件满足其一即可。
    例如：filename:apache content:apache
    4）-条件1 条件2：必须不满足条件1，要满足条件2
    例如：-filename:apache content:apache
    
```
 | Occur.MUST 查询条件必须满足，相当于and  | +（加号） | |
 | :----: | :---: | :---: | 
 |Occur.SHOULD 查询条件可选，相当于or  |空（不用符号）|  | 
 |Occur.MUST_NOT 查询条件不能满足，相当于not非  |-（减号）|  |       

**第二种写法：**

* 条件1 AND 条件2

* 条件1 OR 条件2

* 条件1 NOT 条件2

#### 使用MulitFieldQueryParser查询

* 可以指定多个默认搜索域

* 代码实例

```
    /**
     * MulitFieldQueryParser
     * 可以指定多个默认搜索域
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testMulitFieldQueryParser() throws IOException, ParseException {
        IndexSearcher indexSearcher = getIndexSearcher();
        //可以指定默认多个默认搜索域
        String[] filed = {"name", "content"};
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(filed, new StandardAnalyzer());
        Query query = multiFieldQueryParser.parse("mybatis is apache project");
        System.out.println(query);//(name:mybatis content:mybatis) (name:apache content:apache) (name:project content:project)
        printResult(indexSearcher,query);
    }
```