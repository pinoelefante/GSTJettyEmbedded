package gst.database;

public final class DatabaseSave {

    private boolean collection;
    private Object content;
    private Class<?> contentClass;
    private Runnable runnable;

    private boolean complete;
    private boolean fail;
    private boolean delete;

    public DatabaseSave(Class<?> c, Object content, boolean coll, Runnable r) {
        this.setContent(content);
        this.setContentClass(c);
        this.setCollection(coll);
        this.setRunnable(r);
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Class<?> getContentClass() {
        return contentClass;
    }

    public void setContentClass(Class<?> contentClass) {
        this.contentClass = contentClass;
    }
}