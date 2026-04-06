package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import northern.captain.gamecore.glx.NContext;

public class ScrollTable extends ScrollPane
{
	Table root = new Table();
	
	public interface ListAdapter
	{
		/**
		 * Number of elements in total
		 * @return
		 */
		int getSize();
		
		void setParentTable(ScrollTable tbl);
		
		/**
		 * Adds new row content to the table via creating sub-table and returning it
		 * @param table
		 * @param idx
		 */
		Table addRow(Table table, int idx);
		
		/**
		 * Called by the table when the item is clicked. Passes item idx in the table
		 * @param idx
		 */
		void itemClicked(int idx);

        /**
         * Called by the table when the item is long pressed and held.
         * @param idx
         */
		void itemLongClicked(int idx);
	}
	
	ListAdapter adapter;
	
	public void setAdapter(ListAdapter adapter)
	{
        if(this.adapter != adapter)
            needFill = true;
		this.adapter = adapter;
	}
	
	public ScrollTable(ListAdapter adapter)
	{
		super(null);
		this.adapter = adapter;
		init();
	}

	public ScrollTable(ListAdapter adapter, ScrollPaneStyle style)
	{
		super(null, style);
		init();
		this.adapter = adapter;
	}

	protected void init()
	{
        root.top().left();
		root.defaults().top().expandX().fillX().top();
		setWidget(root);
	}
	
	/**
	 * Return root table set as a widget to this scroll pane
	 * @return
	 */
	public Table getRootTable()
	{
		return root;
	}

	boolean needFill = true;
	
	protected void fillData()
	{
		needFill = false;
        if(adapter == null)
            return;

		root.clear();
        adapter.setParentTable(this);
		for(int i=0, n=adapter.getSize();i<n;i++)
		{
			Table content = adapter.addRow(root, i);
			if(content != null)
			{
				root.add(content).expandX().fillX().top();
                content.setTouchable(Touchable.enabled);
				content.addListener(new RowClickListener(i));
			}
            root.row();
		}
        root.invalidateHierarchy();
        root.pack();
        this.layout();
	}
	
	public void addLastRow()
    {
        int i = adapter.getSize()-1;
        Table content = adapter.addRow(root, i);
        Array<Cell> cells = root.getCells();
        if(cells.size>0)
        {
            Cell cell = cells.get(cells.size-1);
            cell.expand(1, 0);
        }
        if(content != null)
        {
            root.add(content).expand().fillX().top().left();
            content.addListener(new RowClickListener(i));
        }
        root.row();
        root.invalidateHierarchy();
        root.pack();
        this.layout();
    }

    private int refreshCounter = 0;

    public void scrollToEnd()
    {
        setScrollY(getMaxY());
        NContext.current.addTempRefresh();
//        refreshCounter = 120;
    }

    public void scrollToStart()
    {
        setScrollY(0);
        NContext.current.addTempRefresh();
    }

	class RowClickListener extends ClickListener implements Runnable
	{
        private static final long DELAY_LONG_PRESS = 550L;
        private static final long DELAY_LONG_OVERPRESS = 720L;
        private static final long DELAY_LONG_UNDERPRESS = 400L;

        long touchDownTime = 0;

        private boolean isLongPress()
        {
            long curTime = System.currentTimeMillis();
            return curTime - touchDownTime < DELAY_LONG_OVERPRESS && curTime - touchDownTime > DELAY_LONG_UNDERPRESS;
        }


		int idx;
		public RowClickListener(int idx)
		{
			this.idx = idx;
		}
		/* (non-Javadoc)
		 * @see com.badlogic.gdx.scenes.scene2d.utils.ClickListener#clicked(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float)
		 */
		@Override
		public void clicked(InputEvent event, float x, float y)
        {
            if(touchDownTime >= 0)
            {
                adapter.itemClicked(idx);
            }
            touchDownTime = 0;
		}

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
        {
            touchDownTime = System.currentTimeMillis();
            NContext.current.postDelayed(this, DELAY_LONG_PRESS);
            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button)
        {
            super.touchUp(event, x, y, pointer, button);
            touchDownTime = 0;
        }


        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run()
        {
            if(isLongPress())
            {
                touchDownTime = -1;
                adapter.itemLongClicked(idx);
            } else
                touchDownTime = 0;
        }
    }
	
	
	/* (non-Javadoc)
	 * @see com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup#validate()
	 */
	@Override
	public void validate()
	{
		if(needFill)
		{
			fillData();
		}
		super.validate();
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.scenes.scene2d.ui.ScrollPane#draw(com.badlogic.gdx.graphics.g2d.Batch, float)
	 */
	@Override
	public void draw(Batch batch, float parentAlpha)
	{
		if(needFill)
		{
			fillData();
		}
		super.draw(batch, parentAlpha);
        if(isFlinging() || isDragging() || getVisualScrollY()!=getScrollY())
            NContext.current.addTempRefresh();

        if(refreshCounter > 0)
            refreshCounter--;
	}
	
	public void setNeedFill()
	{
		needFill = true;
	}
	
	@Override
	public void pack()
	{
		if(needFill)
			fillData();
		super.pack();
	}
	
}
