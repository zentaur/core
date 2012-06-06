package org.zentaur.core.collections;

/*
 *   Copyright 2012 The Zentaur Server Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static org.zentaur.lang.Objects.eq;
import static org.zentaur.lang.Objects.hash;
import static org.zentaur.lang.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.zentaur.collections.MultiValued;

/**
 * An in-memory based {@link MultiValued} implementation.
 *
 * @param <K> the type of keys maintained by this data structure.
 * @param <V> the type of mapped values.
 */
public final class SimpleMultiValued<K, V>
    implements MultiValued<K, V>
{

    private final Map<K, List<V>> adaptedMap = new HashMap<K, List<V>>();

    /**
     * {@inheritDoc}
     */
    public boolean contains( K key )
    {
        checkArgument( key != null, "null key not admitted" );
        return adaptedMap.containsKey( key );
    }

    /**
     * {@inheritDoc}
     */
    public V getFirstValue( K key )
    {
        checkArgument( key != null, "null key not admitted" );
        List<V> storedValues = adaptedMap.get( key );
        if ( storedValues == null || storedValues.isEmpty() )
        {
            return null;
        }
        return storedValues.iterator().next();
    }

    /**
     * {@inheritDoc}
     */
    public List<V> getValues( K key )
    {
        checkArgument( key != null, "null key not admitted" );
        List<V> storedValues = adaptedMap.get( key );
        if ( storedValues != null )
        {
            return unmodifiableList( storedValues );
        }
        return null;
    }

    /**
     * Allows adding a value in the data structure.
     *
     * @param key the non null mapping key
     * @param value the mapped value
     * @return this data structure instance
     */
    public SimpleMultiValued<K, V> addValue( K key, V value )
    {
        checkArgument( key != null, "null key not admitted" );
        List<V> storedValues = adaptedMap.get( key );
        if ( storedValues == null )
        {
            storedValues = new LinkedList<V>();
            adaptedMap.put( key, storedValues );
        }

        storedValues.add( value );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Set<K> getAllKeys()
    {
        return unmodifiableSet( adaptedMap.keySet() );
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<Entry<K, List<V>>> getAllEntries()
    {
        return new Iterable<Map.Entry<K, List<V>>>()
        {

            public Iterator<Entry<K, List<V>>> iterator()
            {
                return new UnmodifiableIterator<K, V>( adaptedMap.entrySet().iterator() );
            }

        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return hash( 1, 31, adaptedMap );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( obj == null || getClass() != obj.getClass() )
        {
            return false;
        }

        SimpleMultiValued<?, ?> other = (SimpleMultiValued<?, ?>) obj;
        return eq( adaptedMap, other.adaptedMap );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return adaptedMap.toString();
    }

    // internal collections utilities

    /**
     * An iterator implementation from which is impossible remove elements.
     */
    private static final class UnmodifiableIterator<K, V>
        implements Iterator<Entry<K, List<V>>>
    {

        private final Iterator<Entry<K, List<V>>> adapted;

        public UnmodifiableIterator( Iterator<Entry<K, List<V>>> adapted )
        {
            this.adapted = adapted;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext()
        {
            return adapted.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        public Entry<K, List<V>> next()
        {
            return new UnmodifiableEntry<K, V>( adapted.next() );
        }

        /**
         * {@inheritDoc}
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * An entry implementation from which is impossible replace the value
     * and value itself is unmodifiable.
     */
    private static final class UnmodifiableEntry<K, V>
        implements Entry<K, List<V>>
    {

        private final Entry<K, List<V>> adapted;

        public UnmodifiableEntry( Entry<K, List<V>> adapted )
        {
            this.adapted = adapted;
        }

        /**
         * {@inheritDoc}
         */
        public K getKey()
        {
            return adapted.getKey();
        }

        /**
         * {@inheritDoc}
         */
        public List<V> getValue()
        {
            return unmodifiableList( adapted.getValue() );
        }

        /**
         * {@inheritDoc}
         */
        public List<V> setValue( List<V> value )
        {
            throw new UnsupportedOperationException();
        }

    }

}
