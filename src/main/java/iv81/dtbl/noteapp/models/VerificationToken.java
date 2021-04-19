package iv81.dtbl.noteapp.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Document(collection = "verifyTokens")
public class VerificationToken {
    private static final int EXPIRATION = 60*24;

    @Id
    private String id;

    private String token;
    private String userId;

    private Date expiryDate;
    private Date calcExpiryDate(int expiryTimeInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(calendar.getTime().getTime());
    }
}
