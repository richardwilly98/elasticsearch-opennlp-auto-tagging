# elasticsearch-auto-tagging #

Attempt to use OpenNLP library in Elasticsearch
[OpenNLP](http://opennlp.apache.org/)

## How to test ##

### Requirements
- ES 1.0.0.Beta1

#### Install plugin:  
Change $ES_HOME path in rebuild.bat  
Execute rebuild.bat  

#### Create new index  
```PUT /test-autotagging```

#### Create new mapping
```
PUT /test-autotagging/attachment/_mapping
{
  "attachment": {
    "properties": {
      "name": {
        "type": "string"
      },
      "content": {
        "type": "attachment", 
        "fields": {
          "content": {
            "type": "multi_field",
            "fields": {
              "content": {
                "type": "string",
                "term_vector": "with_positions_offsets",
                "store": "yes"
              }
            }
          }
        }
      }
    }
  }
}

```  

#### Index few documents  
```
curl -XPUT localhost:9200/test-autotagging/attachment/1 -d @content-1.json
curl -XPUT localhost:9200/test-autotagging/attachment/2 -d @content-2.json
```

#### Invoke custom REST action to "auto-tag" the indexed documents:
```
POST /test-autotagging/attachment/1/_autoTagging?t=tags&f=content
```

```
PUT /test-autotagging/test/_mapping
{
    "test": {
        "properties": {
            "name": {
                "type": "autotagging", 
                "fields": {
                    "name": {
                        "type": "string",
                        "store"    : "yes"
                    },
                    "tags": {
                        "type": "string",
                        "store"    : "yes"
                    }
                }
            }
        }
    }
}
```

Check the result:
```
GET test-autotagging/attachment/_search
{
    "fields": [
       "name", "content", "tags"
    ], 
   "query": {
       "match_all": {}
   }
}

```   

It should also work as custom type but could slow down indexing process as the operation is asynchronous...  
Index few documents  
```
PUT /test-autotagging/test/1
{ "name": "elastic search richard sample" }
```

```
PUT /test-autotagging/test/2
{ "name": "The quick brown fox jumped over the lazy dog" }
```

Search  
```
GET test-autotagging/test/_search
{
    "fields": [
       "_source", "name.tags"
    ], 
   "query": {
       "match": {
          "name.tags": "search"
       }
   }
}
```

```
GET test-autotagging/test/_search
{
    "fields": [
       "_source", "name.tags"
    ], 
   "query": {
       "match": {
          "name.tags": "dog"
       }
   }
}
```

# License #
```
```

## Resources ##
* [Elasticsearch] (http://www.elasticsearch.org/)
* [mapper-attachments] (https://github.com/elasticsearch/elasticsearch-mapper-attachments)
* [Stanford NLP](http://nlp.stanford.edu/software/tagger.shtml)
