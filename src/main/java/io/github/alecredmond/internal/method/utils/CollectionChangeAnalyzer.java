package io.github.alecredmond.internal.method.utils;

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
  private Set<T> dupesInNew;

  public CollectionChangeAnalyzer(Collection<T> oldCollection, Collection<T> newCollection) {
    this.oldCollection = oldCollection;
    this.newCollection = newCollection;
    this.removed = new HashSet<>(oldCollection);
    this.added = new HashSet<>(newCollection);
    this.common = removed.stream().filter(added::contains).collect(Collectors.toSet());
    removed.removeAll(common);
    added.removeAll(common);
    this.dupesInNew =
        newCollection.stream()
            .filter(t -> Collections.frequency(newCollection, t) > 1)
            .collect(Collectors.toSet());
  }
}
