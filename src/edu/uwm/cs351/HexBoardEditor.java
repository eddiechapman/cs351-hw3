package edu.uwm.cs351;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.uwm.cs351.util.ArrayCollection;

public class HexBoardEditor extends JFrame {
	/**
	 * Eclipse wants this
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		final Collection<HexTile> seq = new ArrayCollection<HexTile>();
		try {
			for (String arg : args) {
				readSeq(seq,new BufferedReader(new FileReader(arg)));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final HexBoardEditor x = new HexBoardEditor(seq);
				x.setSize(800, 500);
				x.setVisible(true);
				// indicate that we don't want automatic close:
				x.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				// if someone closes the window, then we print out all the hex tiles and then
				// dispose the window
				x.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosing(WindowEvent e) {
						for (Iterator<HexTile> it = seq.iterator(); it.hasNext();) {
						    System.out.print(it.next().toString() + "\n");
						}
						x.setVisible(false);
						x.dispose();
					}
					
				});
			}
		});
	}
	
	private static void readSeq(Collection<HexTile> seq, BufferedReader r) throws IOException {
		String input;
		while ((input = r.readLine()) != null) {
			try {
				seq.add(HexTile.fromString(input));
			} catch (FormatException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private final Collection<HexTile> board;
	private HexCoordinate selected  = new HexCoordinate(0,0,0);
	private Terrain currentTerrain = Terrain.INACCESSIBLE;
	private JComboBox<Integer> sizeSelector = new JComboBox<>(new Integer[]{10,15,20,30,50,75,100,150,200,500});
	private JComponent terrainIndicator = new TerrainIndicator();
	
	/**
	 * Return the currently selected hexagon width
	 * @return current hexagon width
	 */
	protected int getHexWidth() {
		return ((Integer)sizeSelector.getSelectedItem()).intValue();
	}

	public HexBoardEditor(final Collection<HexTile> seq) {
		board = seq;
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		final HexPanel hexPanel = new HexPanel();
		contentPane.add(hexPanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		for (Terrain t : Terrain.values()) {
		    JButton b = new JButton(t.toString());
		    b.addActionListener(new ActionListener() {
		        
		        @Override
		        public void actionPerformed(ActionEvent e) {
		            JButton b = (JButton) e.getSource();
		            currentTerrain = Terrain.valueOf(b.getText());
		            terrainIndicator.repaint();
		        }
		        
		    });
		    b.setOpaque(true);
		    b.setBackground(t.getColor());
		    buttonPanel.add(b);
		}
		JPanel indicatorPanel = new JPanel();
		sizeSelector.setSelectedItem(HexTile.WIDTH);
		sizeSelector.addActionListener((ae) -> { hexPanel.repaint(); }); // lambda syntax is SHORT!
		indicatorPanel.add(new JLabel("Current Terrain:"));
		indicatorPanel.add(terrainIndicator);
		indicatorPanel.add(new JLabel("  Hexagon Width:"));
		indicatorPanel.add(sizeSelector);
		contentPane.add(indicatorPanel,BorderLayout.NORTH);
		contentPane.add(buttonPanel,BorderLayout.SOUTH);
		this.setContentPane(contentPane);
		hexPanel.addMouseListener(new MouseAdapter() {
		    
		    @Override
		    public void mouseClicked(MouseEvent e) {
	            selected = HexCoordinate.fromPoint(e.getPoint(), getHexWidth());
	            if (e.getClickCount() == 2) { 
	                for (Iterator<HexTile> it = seq.iterator(); it.hasNext();) {
	                    if (it.next().getLocation().equals(selected)) { 
	                        it.remove(); 
	                    }
	                }
	                if (currentTerrain != Terrain.INACCESSIBLE) { 
	                    seq.add(new HexTile(currentTerrain, selected)); 
	                }
	            }
	            hexPanel.repaint();
	        }

		});
	}
	
	private final class HexPanel extends JPanel {
		/**
		 * Keep Eclipse happy
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g); // render background
			int width = getHexWidth();
			for (Iterator<HexTile> it = board.iterator(); it.hasNext();) {
			    HexTile h = it.next();
			    h.draw(g, width);
			}
			Polygon hexagon = selected.toPolygon(width);
			g.setColor(Color.MAGENTA);
			g.drawPolygon(hexagon);
		}
	}
	
	private class TerrainIndicator extends JLabel {
		/**
		 * Keep Eclipse happy
		 */
		private static final long serialVersionUID = 1L;

		public TerrainIndicator() {
			super("INACCESSIBLE");
			this.setOpaque(true); // need this on some systems to activate color
			this.setBackground(Color.BLACK);
			this.setHorizontalAlignment(SwingConstants.CENTER);
		}
		
		@Override
		public void repaint() {
			this.setText(currentTerrain.toString());
			this.setBackground(currentTerrain.getColor());
			super.repaint();
		}
	}
	
}
