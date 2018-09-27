package com.haoxy.lucene.creatIndex;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by haoxy on 2018/9/21.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 * <p>
 * 索引库的查询
 */
public class IndexSearcherLucene {

    public IndexSearcher getIndexSearcher() throws IOException {
        //1, 指定索引库存储的位置
        Directory directory = FSDirectory.open(new File("/tmp/index/"));
        //2,使用IndexReade 对象打开索引库
        IndexReader indexReader = DirectoryReader.open(directory);
        //3,创建一个IndexSearcher 对象 ,构造方法中需要 IndexReade 参数
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        return indexSearcher;
    }

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

    /**
     * 使用query的子类进行查询
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
}
