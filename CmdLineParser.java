import org.apache.commons.cli.*;

public class CmdLineParser {
    public String[] parse_options(String[] args) {
        String data[] = new String[7];

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        options.addOption("peers", "all", true, "Port");
        options.addOption("s", "almost-all", true, "IP");
        options.addOption(Option.builder("t").optionalArg(true).hasArgs().build());
        options.addOption(Option.builder("m").optionalArg(true).hasArgs().build());
        options.addOption("T", "all", false, "TCP");
        options.addOption("U", "almost-all", false, "UDP");
        options.addOption("d", "all", true, "Database");

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("peers")) {
                data[0] = line.getOptionValues("peers")[0];
            }

            if (line.hasOption("s")) {
                data[1] = line.getOptionValues("s")[0];
            }

            if (line.hasOption("t")) {
                if ((line.getOptionValues("t") != null)) {
                    data[3] = line.getOptionValues("t")[0];
                }
            }

            if (line.hasOption("m")) {
                if ((line.getOptionValues("m") != null)) {
                    data[2] = String.join(" ", line.getOptionValues("m"));
                }
            }

            if (line.hasOption("T")) {
                data[4] = "TCP";
            }

            if (line.hasOption("U")) {
                data[5] = "UDP";
            }

            if (line.hasOption("d")) {
                data[6] = line.getOptionValues("d")[0];
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e);
        }

        return data;
    }
}