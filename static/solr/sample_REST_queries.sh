curl "http://localhost:8983/solr/Sample-07-Collection_shard1_replica1/select?q=description%3D%22Chief+executives%22&wt=csv&indent=true"
curl "http://localhost:8983/solr/Sample-07-Collection_shard1_replica1/select?q=code%3D%2211-1011%22&wt=csv&indent=true"

echo
echo case insenstive and wildcard search for 'manager*'
echo curl "http://localhost:8983/solr/Sample-07-Collection_shard1_replica1/select?q=description:manager*&wt=csv&indent=true"
curl "http://localhost:8983/solr/Sample-07-Collection_shard1_replica1/select?q=description:manager*&wt=csv&indent=true"
echo
echo search for two terms
echo curl 'http://localhost:8983/solr/Sample-07-Collection_shard1_replica1/select?q=description:%28computer+AND+engineers%29&wt=csv&indent=true'
curl 'http://localhost:8983/solr/Sample-07-Collection_shard1_replica1/select?q=description:%28computer+AND+engineers%29&wt=csv&indent=true'
echo
echo proximity search for computer and engineers within 4 words
echo curl 'http://localhost:8983/solr/Sample-07-Collection_shard1_replica1/select?q=description:%22computer+engineers%22~4&wt=csv&indent=true'
curl 'http://localhost:8983/solr/Sample-07-Collection_shard1_replica1/select?q=description:%22computer+engineers%22~4&wt=csv&indent=true'