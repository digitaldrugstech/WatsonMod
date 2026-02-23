package eu.minemania.watson.analysis;

import java.util.regex.Matcher;
import net.minecraft.text.MutableText;

/**
 * An {@link Analysis} implementation for AuxProtect query results.
 *
 * Pattern handlers will be added once AuxProtect chat output format
 * is captured from a test server.
 */
public class AuxProtectAnalysis extends Analysis
{
    public AuxProtectAnalysis()
    {
        // TODO: Add pattern handlers when AuxProtect chat format is known
        // Expected patterns: AP_HEADER, AP_DETAILS, AP_COORDS, AP_PAGE
    }
}
