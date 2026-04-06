package northern.captain.quadronia.game;

import northern.captain.tools.Helpers;

/**
 * Created by leo on 05.03.15.
 */
public class Item
{
    public int idx;
    public int mode;
    public Element[] elements;

    public Item(int numElements)
    {
        elements = new Element[numElements];
    }

    public Item()
    {
        this(3);
    }

    public Item(int[] nodes, int idx, int mode)
    {
        this(nodes.length/2);
        this.idx = idx;
        this.mode = mode;
        for(int i=0;i<elements.length;i++)
        {
            elements[i] = new Element(nodes[i*2], nodes[i*2+1]);
            elements[i].setParentItem(this);
        }
    }

    public Item startWith(int startWith)
    {
        elements[0] = new Element(startWith);
        elements[0].setParentItem(this);
        return generate();
    }

    public Item generate()
    {
        if(elements[0] == null)
        {
            elements[0] = new Element(Helpers.RND.nextInt(Face.FACE_MAX));
        }

        Element prev = elements[0];
        for(int i = 1;i<elements.length;i++)
        {
            Element element = new Element(Face.SIDE_CONNECTOR[prev.sides[Element.TWO]]);
            element.setParentItem(this);
            prev = element;
            elements[i] = element;
        }

        return this;
    }

}
