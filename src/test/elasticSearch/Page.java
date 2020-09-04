package elasticSearch;


/**
 * @author laiyuan
 * @date 2020/8/31
 */
public class Page {
    private Integer size;
    private Integer current;
    private Integer from;

    public Page() {
        this.size = 10;
        this.current = 1;
        this.from = 0;
    }

    public Page(Integer size, Integer current) {
        this.size = size;
        this.current = current;
        this.from = size * (current - 1);
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }
}
