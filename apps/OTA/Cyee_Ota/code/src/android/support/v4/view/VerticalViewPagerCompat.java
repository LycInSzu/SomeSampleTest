package android.support.v4.view;

public final class VerticalViewPagerCompat {
	private VerticalViewPagerCompat() {
	}
/**
 * 
 * @author cuijiuyu
 *
 */
	public interface DataSetObserver extends PagerAdapter.DataSetObserver {
	}

	public static void setDataSetObserver(PagerAdapter adapter,
			DataSetObserver observer) {
		adapter.setDataSetObserver(observer);
	}
}
