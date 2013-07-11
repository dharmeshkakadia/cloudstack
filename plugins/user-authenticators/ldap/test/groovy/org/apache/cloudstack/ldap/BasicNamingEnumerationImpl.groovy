package groovy.org.apache.cloudstack.ldap

import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.SearchResult

class BasicNamingEnumerationImpl implements NamingEnumeration {

    private LinkedList<String> items = new LinkedList<SearchResult>();

    @Override
    public boolean hasMoreElements() {
        return items.size != 0;
    }

    @Override
    public Object nextElement() {
        SearchResult result = items.getFirst();
        items.removeFirst();
        return result;
    }

    @Override
    public void close() throws NamingException {
    }

    @Override
    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }

    @Override
    public Object next() throws NamingException {
        return nextElement();
    }

    public void add(SearchResult item) {
        items.add(item)
    }
}
