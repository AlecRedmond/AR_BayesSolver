package io.github.alecredmond.internal.method.network.changehandlers;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class CollectionChangeAnalyzer<T> {
  private Collection<T> oldCollection;
  private Collection<T> newCollection;
  private Set<T> removed;
  private Set<T> added;
  private Set<T> common;
  private Set<T> dupesInNewCollection;

  public CollectionChangeAnalyzer(Collection<T> oldCollection, Collection<T> newCollection) {
    this.oldCollection = oldCollection;
    this.newCollection = newCollection;
    this.removed = new HashSet<>(oldCollection);
    this.added = new HashSet<>(newCollection);
    this.common = removed.stream().filter(added::contains).collect(Collectors.toSet());
    removed.removeAll(common);
    added.removeAll(common);
    this.dupesInNewCollection =
        newCollection.stream()
            .filter(t -> Collections.frequency(newCollection, t) > 1)
            .collect(Collectors.toSet());
  }

  @SuppressWarnings("unchecked")
  public static <T> CollectionChangeAnalyzer<T> of(PropertyChangeEvent evt) {
    return new CollectionChangeAnalyzer<>(
        (Collection<T>) evt.getOldValue(), (Collection<T>) evt.getNewValue());
  }
}
