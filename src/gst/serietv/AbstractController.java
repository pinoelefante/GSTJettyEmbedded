package gst.serietv;

import java.util.List;
import java.util.Set;

public abstract class AbstractController<S extends SerieTV, ID>
{
	public abstract List<S> aggiornaSerie();
	public abstract List<S> elencoSerie();
	public abstract Set<EpisodeWrapper> aggiornaEpisodi(S s);
	public abstract Set<EpisodeWrapper> aggiornaEpisodi(ID id);
	public abstract Set<EpisodeWrapper> elencoEpisodi(S s);
	public abstract Set<EpisodeWrapper> elencoEpisodi(ID id);
	public abstract S getSerie(ID id);
}
