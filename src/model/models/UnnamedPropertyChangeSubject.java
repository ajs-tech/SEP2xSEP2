package model.models;

import java.beans.PropertyChangeListener;

public interface UnnamedPropertyChangeSubject {
    public void addPropertyChangeListener(PropertyChangeListener pcl);
    public void removePropertyChangeListener(PropertyChangeListener pcl);
}
