
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import javax.imageio.ImageIO;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class FractalExplorer extends JFrame
{
	private int size;
	public JImageDisplay imageDisp;
	private FractalGenerator generator;
	private Rectangle2D.Double range;
	
	private int rowsRemaining;
	
	private JFrame frame;
	private JButton reset;
	private JButton save;
	private JPanel buttons;
	private JComboBox<FractalGenerator> box;
	private JLabel label;
	private JPanel panel;
	
	public FractalExplorer(int size)
	{
		this.size = size;
		this.range = new Rectangle2D.Double();
		this.generator = new Mandelbrot();
		this.generator.getInitialRange(range);
	}
	
	public void createAndShowGUI()
	{
		frame = new JFrame("Fractal");
        reset = new JButton("Reset");
		save = new JButton("Save Image");
		buttons = new JPanel();
		buttons.add(save);
		buttons.add(reset);
		
		box = new JComboBox<>();
		box.addItem(new Mandelbrot());
		box.addItem(new Tricorn());
		box.addItem(new BurningShip());
		label = new JLabel("Fractal: ");
		panel = new JPanel();
		panel.add(label);
		panel.add(box);
		
		imageDisp = new JImageDisplay(size,size);
		
		ResetEvent resetEvent = new ResetEvent();
        MouseHandler mouseHandler = new MouseHandler();
		imageDisp.setLayout(new BorderLayout());
		
		ChangeEvent changeEvent = new ChangeEvent();
		SaveEvent saveEvent = new SaveEvent();
		
		imageDisp.addMouseListener(mouseHandler);
        reset.addActionListener(resetEvent);
		box.addActionListener(changeEvent);
		
		save.addActionListener(saveEvent);
		
		frame.add(imageDisp, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.NORTH);
		frame.add(buttons, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		
	}
	
	private void drawFractal()
	{
		this.enableUI(false);
		rowsRemaining = size;
        for(int y = 0; y < size; y++)
		{
			FractalWorker worker = new FractalWorker(y);
			worker.execute();
		}
		//imageDisp.repaint();
	}
	
	private class ResetEvent implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            generator.getInitialRange(range);
            drawFractal();
        }
    }
	
	private class ChangeEvent implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
			JComboBox box = (JComboBox)e.getSource();
			String type = box.getSelectedItem().toString();
			if(type == "Mandelbrot")
			{
				generator = new Mandelbrot();
			}
			if(type == "Tricorn")
			{
				generator = new Tricorn();
			}
			if(type == "Burning Ship")
			{
				generator = new BurningShip();
			}
            generator.getInitialRange(range);
            drawFractal();
        }
    }
	
	private class SaveEvent implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser(); 
			FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png"); 
			chooser.setFileFilter(filter); 
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.showSaveDialog(null);
			try
			{
				ImageIO.write(imageDisp.buffImg, "png", chooser.getSelectedFile());
			}
			catch(IOException io)
			{
				JOptionPane.showMessageDialog(null, io.getMessage(), "Can not Save Image", JOptionPane.ERROR_MESSAGE);
			}
			catch(IllegalArgumentException ilArg)
			{
				
			}
        }
    }
	
	private class MouseHandler extends MouseAdapter{
        
        public void mouseClicked(MouseEvent e)
        {
			if(rowsRemaining == 0)
			{
				int x = e.getX();
				int y = e.getY();
				double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, size, x);
				double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, size, y);
				generator.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
				drawFractal();
			}
        }
    }
	
	private class FractalWorker extends SwingWorker<Object, Object>
	{
		private int lineY;
		//private int[] pixelsRGB;
		
		public FractalWorker(int y)
		{
			this.lineY = y;
		}
		
		protected Object doInBackground()
		{
			//this.pixelsRGB = new int[800];
			for(int x = 0; x < size; x++)
			{
					double xCoord  =  FractalGenerator.getCoord(range.x,  range.x  +  range.width, size, x);
					double yCoord  =  FractalGenerator.getCoord(range.y,  range.y  +  range.height, size, lineY);
					int iteration = generator.numIterations(xCoord,yCoord);
					if (iteration == -1)
					{
						imageDisp.drawPixel(x,lineY,0);
						//this.pixelsRGB[x] = 0;
					}
					else
					{
						float hue = 0.7f + (float) iteration / 200f; 
						int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
						imageDisp.drawPixel(x,lineY,rgbColor);
						//this.pixelsRGB[x] = rgbColor;
					}
				
			}
			return null;
		}
		
		protected void done()
		{
			imageDisp.repaint(0, 0, lineY, size, 1);
			rowsRemaining--;
			if(rowsRemaining == 0)
			{
				enableUI(true);
			}
		}
		
	}
	
	public void enableUI(boolean val)
	{
		reset.setEnabled(val);
		save.setEnabled(val);
		box.setEnabled(val);
	}
	
	public static void main(String[] args) {
        FractalExplorer fractalExplorer = new FractalExplorer(800);
        fractalExplorer.createAndShowGUI();
        fractalExplorer.drawFractal();
    }
	
}