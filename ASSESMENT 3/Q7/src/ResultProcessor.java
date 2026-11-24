import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

/**
 * Task worker processing individual student score file
 */
public class ResultProcessor implements Callable<StudentScore> {
    private final String studentId;
    private final int payloadType; // 0: Normal, 1: Missing File, 2: Corrupted Data

    public ResultProcessor(String studentId, int payloadType) {
        this.studentId = studentId;
        this.payloadType = payloadType;
    }

    @Override
    public StudentScore call() {
        try {
            // Simulate reading and parsing student score file
            if (payloadType == 1) {
                throw new FileNotFoundException("Score file missing on disk: score_" + studentId + ".dat");
            }
            if (payloadType == 2) {
                throw new IllegalArgumentException("Corrupt score payload: unparseable characters in score_" + studentId + ".dat");
            }

            // Simulate CPU processing time (score weighting and statistical normalization)
            int calculatedScore = 50 + Math.abs(studentId.hashCode() % 51); // Score between 50 and 100
            return StudentScore.success(studentId, calculatedScore);

        } catch (FileNotFoundException e) {
            // Fault-tolerant catch: Isolates file absence from crashing the executor
            return StudentScore.failure(studentId, StudentScore.Status.MISSING_FILE, e.getMessage());

        } catch (IllegalArgumentException e) {
            // Fault-tolerant catch: Isolates corrupted record
            return StudentScore.failure(studentId, StudentScore.Status.CORRUPT_DATA, e.getMessage());

        } catch (Throwable t) {
            // Catch-all safety net
            return StudentScore.failure(studentId, StudentScore.Status.SYSTEM_ERROR, t.getMessage());
        }
    }
}
