import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.DateFormat;

public class Solution {
    private final String filename = "calendar.csv";
    private final Double ONEDAY = 86400000.0;
    private final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<List<TimeFrag>> week = new ArrayList<>(8);
    private Scanner in;
    private Calendar rangeStart = new GregorianCalendar();
    private Calendar rangeEnd;



    public static void main(String[] args) {
        try {
            Solution sln = new Solution();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    Solution() throws Exception {
        File file = new File(filename);
        in = new Scanner(file);
        Calendar currentTimeStart = new GregorianCalendar();
        Calendar currentTimeEnd = new GregorianCalendar();
        currentTimeStart.set(Calendar.HOUR_OF_DAY, 8);
        currentTimeEnd.set(Calendar.HOUR_OF_DAY, 22);
        currentTimeStart.set(Calendar.MINUTE, 0);
        currentTimeEnd.set(Calendar.MINUTE, 0);
        currentTimeStart.set(Calendar.SECOND, 0);
        currentTimeEnd.set(Calendar.SECOND, 0);
        for (int i = 0; i < 8; i++) {
            Calendar timestart = (Calendar)currentTimeStart.clone();
            Calendar timeend = (Calendar)currentTimeEnd.clone();
            timestart.add(Calendar.DAY_OF_MONTH, i);
            timeend.add(Calendar.DAY_OF_MONTH, i);
            TimeFrag fragment = new TimeFrag(timestart, timeend);
            week.add(new ArrayList<TimeFrag>());
            week.get(i).add(fragment);
        }

        rangeEnd = currentTimeEnd;
        rangeEnd.add(Calendar.DAY_OF_MONTH, 7);

        if (rangeStart.after(currentTimeEnd)) {
            week.get(0).clear();
        }
        else if (rangeStart.after(currentTimeStart)){
            TimeFrag fstFrag = new TimeFrag(rangeStart, currentTimeEnd);
            week.get(0).clear();
            week.get(0).add(fstFrag);
        }

        readInput();

        for (int i = 0; i < 8; i++) {
            Collections.sort(week.get(i));
        }

        printResult();


    }

    private void readInput() throws Exception {
        while (in.hasNextLine()) {
            String schedule = in.nextLine();
            System.out.println(schedule);
            Calendar[] range = parseSchedule(schedule);
            if (range[1].after(rangeStart) && range[0].before(rangeEnd)) {
                checkTimeTable(new TimeFrag(range[0], range[1]));
            }
        }
    }

    private void printResult() {
        for (int i = 0; i < 8; i++) {
            System.out.printf("Day %d: ", i);
            if (week.get(i).isEmpty()) {
                System.out.println("No available time");
            }
            else {
                String result = week.get(i).get(0).fragToString();
                System.out.println(result);
            }
        }
    }

    private void checkTimeTable(TimeFrag frag) {
        Integer startDay = getWhichDay(rangeStart, frag.getStart());
        Integer endDay = getWhichDay(rangeStart, frag.getEnd());

        if (startDay == endDay) {
            updateDay(startDay, frag);
        }
        else if (startDay < 0) {
            for (int i = 0; i < endDay; i++) {
                week.get(i).clear();
            }
            updateDay(endDay, frag);
        }
        else if (endDay > 7) {
            for (int i = startDay + 1; i <= 7; i++) {
                week.get(i).clear();
            }
            updateDay(startDay, frag);
        }
        else {
            for (int i = startDay + 1; i < endDay; i++) {
                week.get(i).clear();
            }
            updateDay(startDay, frag);
            updateDay(endDay,frag);
        }
    }

    private void updateDay(Integer day, TimeFrag frag) {
        ListIterator<TimeFrag> it = week.get(day).listIterator();
        while (it.hasNext()) {
            TimeFrag curFrag = it.next();
            if (frag.noOverlap(curFrag)) continue;
            else if (frag.coverOverlap(curFrag)) it.remove();
            else if (frag.startOverlap(curFrag)) {
                curFrag.setStart(frag.getEnd());
                it.remove();
                it.add(curFrag);
            }
            else if (frag.endOverlap(curFrag)) {
                curFrag.setEnd(frag.getStart());
                it.remove();
                it.add(curFrag);
            }
            else if (frag.withInOverlap(curFrag)) {
                TimeFrag stFrag = new TimeFrag(curFrag.getStart(), frag.getStart());
                TimeFrag edFrag = new TimeFrag(frag.getEnd(), curFrag.getEnd());
                it.remove();
                it.add(stFrag);
                it.add(edFrag);
            }
        }
    }

    private Calendar[] parseSchedule(String schedule) throws Exception {
        Calendar[] range = new Calendar[2];
        range[0] = new GregorianCalendar();
        range[1] = new GregorianCalendar();
        String[] ss = schedule.split(",");
        range[0].setTime(fmt.parse(ss[1]));
        range[1].setTime(fmt.parse(ss[2]));
        return range;
    }

    private Integer getWhichDay(Calendar c1, Calendar c2) {
        Double result = ((c2.getTimeInMillis() - c1.getTimeInMillis()) / ONEDAY);
        return result.intValue();
    }

    private class TimeFrag implements Comparable<TimeFrag> {
        private Calendar start;
        private Calendar end;
        private Long duration;

        TimeFrag(String schedule) throws Exception {
            Calendar[] range = parseSchedule(schedule);
            this.start = range[0];
            this.end = range[1];
            this.duration = this.end.getTimeInMillis() - this.start.getTimeInMillis();
        }

        TimeFrag(Calendar start, Calendar end) {
            this.start = start;
            this.end = end;
            this.duration = this.end.getTimeInMillis() - this.start.getTimeInMillis();
        }

        @Override
        public int compareTo(TimeFrag other) {
            return Long.compare(other.duration, duration);
        }

        private Boolean coverOverlap(TimeFrag other) {
            return (start.before(other.start) && end.after(other.end));
        }

        private Boolean withInOverlap(TimeFrag other) {
            return (start.after(other.start) && end.before(other.end));
        }

        private Boolean startOverlap(TimeFrag other) {
            return (start.before(other.start) && end.before(other.end));
        }

        private Boolean endOverlap(TimeFrag other) {
            return (start.after(other.start) && end.after(other.end));
        }

        private Boolean noOverlap(TimeFrag other) {
            return (end.before(other.start) || start.after(other.end));
        }

        private Calendar getStart() {
            return this.start;
        }

        private Calendar getEnd() {
            return this.end;
        }

        private void setStart(Calendar newst) {
            this.start = newst;
            this.duration = this.end.getTimeInMillis() - this.start.getTimeInMillis();
        }

        private void setEnd(Calendar newed) {
            this.end = newed;
            this.duration = this.end.getTimeInMillis() - this.start.getTimeInMillis();
        }

        private String fragToString() {
            return fmt.format(start.getTime()) + ", " + fmt.format(end.getTime());
        }
    }

}
