package org.mrumrocks.towerdefgen.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class SimpleListModel<E> implements ListModel<E> {

	private final EventListenerList listenerList = new EventListenerList();

	private List<E> elements = new ArrayList<E>();

	@Override
	public void addListDataListener(ListDataListener ldl) {
		listenerList.add(ListDataListener.class, ldl);
	}

	public E remove(int index) {
		for (ListDataListener ldl : listenerList
				.getListeners(ListDataListener.class)) {
			ldl.intervalRemoved(new ListDataEvent(this,
					ListDataEvent.INTERVAL_REMOVED, index, index));
		}
		return elements.remove(index);
	}

	public boolean isEmpty() {
		return elements.isEmpty();
	}

	public int size() {
		return elements.size();
	}

	public boolean remove(Object o) {
		for (ListDataListener ldl : listenerList
				.getListeners(ListDataListener.class)) {
			ldl.intervalRemoved(new ListDataEvent(this,
					ListDataEvent.INTERVAL_REMOVED, elements.indexOf(o),
					elements.indexOf(o)));
		}
		return elements.remove(o);
	}

	@Override
	public E getElementAt(int index) {
		return elements.get(index);
	}

	@Override
	public int getSize() {
		return elements.size();
	}

	@Override
	public void removeListDataListener(ListDataListener ldl) {
		listenerList.remove(ListDataListener.class, ldl);
	}

	public boolean add(E e) {
		for (ListDataListener ldl : listenerList
				.getListeners(ListDataListener.class)) {
			ldl.intervalRemoved(new ListDataEvent(this,
					ListDataEvent.INTERVAL_ADDED, elements.size(), elements
							.size()));
		}
		return elements.add(e);
	}

	public void clear() {
		elements.clear();
	}

	public void moveUp(int index) {
		Collections.swap(elements, index, index - 1);
		for (ListDataListener ldl : listenerList
				.getListeners(ListDataListener.class)) {
			ldl.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, index - 1, index));
		}
	}

	public void moveDown(int index) {
		Collections.swap(elements, index, index + 1);
		for (ListDataListener ldl : listenerList
				.getListeners(ListDataListener.class)) {
			ldl.contentsChanged(new ListDataEvent(this,
					ListDataEvent.CONTENTS_CHANGED, index, index + 1));
		}
	}
}
