package com.fasterxml.jackson.dataformat.cbor.parse;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.cbor.CBORTestBase;

public class ParserDupHandlingTest extends CBORTestBase
{
    public void testSimpleDups() throws Exception
    {
        for (String json : new String[] {
                "{ \"a\":1, \"a\":2 }",
                "[{ \"a\":1, \"a\":2 }]",
                "{ \"a\":1, \"b\":2, \"c\":3,\"a\":true,\"e\":false }",
                "{ \"foo\": { \"bar\": [ [ { \"x\":3, \"a\":1 } ]], \"x\":0, \"a\":\"y\", \"b\":3,\"a\":13 } }",
        }) {
            byte[] doc = cborDoc(json);
            ObjectReader r = sharedMapper().reader();
            _testSimpleDupsOk(doc, r.without(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
            _testSimpleDupsFail(doc,
                    r.with(JsonParser.Feature.STRICT_DUPLICATE_DETECTION),
                    "a");
        }
    }

    private void _testSimpleDupsOk(final byte[] doc, ObjectReader r) throws Exception
    {
        JsonParser p = r.createParser(doc);
        JsonToken t = p.nextToken();
        assertNotNull(t);
        assertTrue(t.isStructStart());
        while (p.nextToken() != null) { }
        p.close();
    }

    private void _testSimpleDupsFail(final byte[] doc, ObjectReader r, String name) throws Exception
    {
        JsonParser p = r.createParser(doc);
        JsonToken t = p.nextToken();
        assertNotNull(t);
        assertTrue(t.isStructStart());
        try {
            while (p.nextToken() != null) { }
            fail("Should have caught dups in document: "+doc);
        } catch (JsonParseException e) {
            verifyException(e, "duplicate field '"+name+"'");
        }
        p.close();
    }
}
