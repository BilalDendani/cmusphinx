/**
 * [[[copyright]]]
 */

package tests.frontend;

import edu.cmu.sphinx.frontend.CepstralMeanNormalizer;
import edu.cmu.sphinx.frontend.Cepstrum;
import edu.cmu.sphinx.frontend.CepstrumFrame;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataSource;
import edu.cmu.sphinx.frontend.Feature;
import edu.cmu.sphinx.frontend.FeatureExtractor;
import edu.cmu.sphinx.frontend.FeatureFrame;
import edu.cmu.sphinx.frontend.SegmentEndPointSignal;
import edu.cmu.sphinx.frontend.Signal;
import edu.cmu.sphinx.frontend.Util;
import edu.cmu.sphinx.util.Timer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;


/**
 * Test program for the CepstralMeanNormalizer and the FeatureExtractor.
 * Reads in a file of CepstrumFrames, and test the process() method of
 * both processors on each Frame.
 */
public class CmnFeatureExtractorTest implements DataSource {

    private static final String CEPSTRUM_FRAME = "CEPSTRUM_PRODUCER";
    private static final String CEPSTRUM = "CEPSTRUM";

    private BufferedReader reader;
    private CepstralMeanNormalizer cmn;
    private FeatureExtractor featureExtractor;
    private boolean start = true;
    private String line;
    private boolean dumpValues;
    private boolean dumpTimes;


    /**
     * Constructs a CmnFeatureExtractorTest with the given cepstra
     * input file.
     *
     * @param cepstrumFile a cepstra input file
     */
    public CmnFeatureExtractorTest(String cepstrumFile) throws IOException {
	this.reader = new BufferedReader(new FileReader(cepstrumFile));
	
        dumpValues = Boolean.getBoolean
            ("tests.frontend.CmnFeatureExtractorTest.dumpValues");
        dumpTimes = Boolean.getBoolean
            ("tests.frontend.CmnFeatureExtractorTest.dumpTimes");
        
        cmn = new CepstralMeanNormalizer();
	cmn.setSource(this);
	featureExtractor = new FeatureExtractor();
	featureExtractor.setSource(cmn);
	featureExtractor.setDump(dumpValues);
        start = true;
    }


    /**
     * Runs the CmnFeatureExtractorTest.
     */
    public void run() throws IOException {
	Data result = null;
	FeatureFrame featureFrame = null;

        // produce as many FeatureFrames as possible
	do {
	    result = featureExtractor.read();
        } while (result != null);

        if (dumpTimes) {
            Timer.dumpAll("");
        }
    }


    /**
     * Returns the next available cepstrum frame, or null if no more
     * cepstrum frames.
     *
     * @returns the next available cepstrum frame, or null
     */ 
    public Data read() throws IOException {
	Data frame = null;

	if (start) {
	    line = reader.readLine();
	}
	
	if (line != null && line.startsWith(CEPSTRUM_FRAME)) {
	    int numberCepstrum = Integer.parseInt
		(line.substring(line.lastIndexOf(' ') + 1));
	    
	    Cepstrum[] cepstra = new Cepstrum[numberCepstrum];
            
	    for (int i = 0; i < numberCepstrum; i++) {
		cepstra[i] = readNextCepstrum();
	    }
	    
	    CepstrumFrame cepstrumFrame = new CepstrumFrame(cepstra);
	    
	    line = reader.readLine();

	    if (start) { // start segment
		frame = SegmentEndPointSignal.getStartSignal(cepstrumFrame);
		start = false;
	    } else if (line == null) { // end segment
                frame = SegmentEndPointSignal.getEndSignal(cepstrumFrame);
            } else { // middle
                frame = cepstrumFrame;
            }
	}
    
	return frame;
    }


    /**
     * Reads and returns the next Cepstrum from the InputStream.
     *
     * @return the next Cepstrum
     */
    private Cepstrum readNextCepstrum() throws IOException {
        // read one Cepstrum
        String line = reader.readLine();
        String[] data = line.split("\\s+");
        
        int cepstrumLength = Integer.parseInt(data[1]);
        float[] cepstrumData = new float[cepstrumLength];
        for (int j = 2; j < data.length; j++) {
            cepstrumData[j-2] = Float.parseFloat(data[j]);
        }
        return (new Cepstrum(cepstrumData));
    }



    /**
     * Main method for the CmnFeatureExtractorTest.
     *
     * @param argv expects one argument, the name of the Cepstra file
     */
    public static void main(String[] argv) {
	try {
	    CmnFeatureExtractorTest test = 
		new CmnFeatureExtractorTest(argv[0]);
	    test.run();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
    }
}
