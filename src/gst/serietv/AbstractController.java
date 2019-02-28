package gst.serietv;

import java.util.List;
import java.util.SortedSet;

public abstract class AbstractController<S extends SerieTV, E extends Episodio, ID>
{
	public abstract List<S> aggiornaSerie();
	public abstract List<S> elencoSerie();
	public abstract SortedSet<E> aggiornaEpisodi(S s);
	public abstract SortedSet<E> aggiornaEpisodi(ID id);
	public abstract SortedSet<E> elencoEpisodi(S s);
	public abstract SortedSet<E> elencoEpisodi(ID id);
	public abstract S getSerie(ID id);
}
