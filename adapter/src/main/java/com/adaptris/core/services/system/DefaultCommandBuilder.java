package com.adaptris.core.services.system;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default implementation of {@link CommandBuilder}
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("default-system-command-builder")
public class DefaultCommandBuilder implements CommandBuilder {

  @NotNull
  @AutoPopulated
  private List<String> environmentMetadataKeys;
  @NotNull
  @AutoPopulated
  private KeyValuePairSet environmentProperties;
  @NotNull
  @AutoPopulated
  private List<CommandArgument> arguments;
  @NotBlank
  private String executablePath;
  private String workingDirectory;
  private Boolean quoteHandling;
  private Integer successExitCode;

  public DefaultCommandBuilder() {
    setEnvironmentMetadataKeys(new ArrayList<String>());
    setEnvironmentProperties(new KeyValuePairSet());
    setArguments(new ArrayList<CommandArgument>());
  }

  public CommandLine createCommandLine(AdaptrisMessage msg) {
    CommandLine commandLine = new CommandLine(getExecutablePath());
    for (CommandArgument argument : getArguments()) {
      commandLine.addArgument(argument.retrieveValue(msg), quoteHandling());
    }
    return commandLine;
  }

  public Map<String, String> createEnvironment(AdaptrisMessage msg) {
    Map<String, String> env = new HashMap<String, String>();
    for (KeyValuePair kvp : getEnvironmentProperties()) {
      env.put(kvp.getKey(), kvp.getValue());
    }
    for (String key : environmentMetadataKeys) {
      if (msg.containsKey(key)) {
        env.put(key, msg.getMetadataValue(key));
      }
    }
    return env.size() == 0 ? null : env;
  }

  public Executor configure(Executor exe) {
    if (!isEmpty(getWorkingDirectory())) {
      File wd = new File(getWorkingDirectory());
      exe.setWorkingDirectory(wd);
    }
    exe.setExitValue(successExitValue());
    return exe;
  }

  public List<String> getEnvironmentMetadataKeys() {
    return environmentMetadataKeys;
  }

  /**
   * Specifies any metadata keys that should be specified as Environment Variables.
   * <p>
   * Each key, if available as metadata, will become the environment variable with each corresponding value, the value.
   * </p>
   * 
   * @param l
   */
  public void setEnvironmentMetadataKeys(List<String> l) {
    if (l == null) {
      throw new IllegalArgumentException("Null Environment Metadata Keys");
    }
    environmentMetadataKeys = l;
  }

  public void addEnvironmentMetadataKey(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Null Environment Metadata Key");
    }
    environmentMetadataKeys.add(key);
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  /**
   * Specify the working directory for the process.
   * 
   * @param wd the working directory; defaults to null
   */
  public void setWorkingDirectory(String wd) {
    this.workingDirectory = wd;
  }

  public KeyValuePairSet getEnvironmentProperties() {
    return environmentProperties;
  }

  /**
   * Specifies any fixed value environment variables that are necessary.
   * 
   * @param env
   */
  public void setEnvironmentProperties(KeyValuePairSet env) {
    if (env == null) {
      throw new IllegalArgumentException("Null Environment Properties");
    }
    this.environmentProperties = env;
  }

  public String getExecutablePath() {
    return executablePath;
  }

  /**
   * Specifies the executable to be invoke.
   * <p>
   * This can be an absolute path or else simply the name of an executable on the PATH. Examples might be "/bin/ls" or "echo" on
   * Unix. Note that this defaults to null, which might create undefined behaviour.
   * </p>
   * 
   * @param executable
   */
  public void setExecutablePath(String executable) {
    this.executablePath = executable;
  }

  public List<CommandArgument> getArguments() {
    return arguments;
  }

  /**
   * Specifies a list of command line arguments to be passed into the executable
   * 
   * @param l the arguments
   */
  public void setArguments(List<CommandArgument> l) {
    if (l == null) {
      throw new IllegalArgumentException("Command arguments are null");
    }
    arguments = l;
  }

  public void addArgument(CommandArgument arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Command argument is null");
    }
    arguments.add(arg);
  }


  public Integer getSuccessExitCode() {
    return successExitCode;
  }

  /**
   * Set the exit code value that is considered successful.
   * 
   * @param i the exit code that is successful; defaults to 0 if not explicitly set.
   */
  public void setSuccessExitCode(Integer i) {
    this.successExitCode = i;
  }

  int successExitValue() {
    return getSuccessExitCode() != null ? getSuccessExitCode().intValue() : 0;
  }

  public Boolean getQuoteHandling() {
    return quoteHandling;
  }

  /**
   * Specify whether to handle quotes or not.
   * <p>
   * If any argument doesn't include spaces or quotes, return it as is. If it contains double quotes, use single quotes - else
   * surround the argument by double quotes.
   * </p>
   * 
   * @see CommandLine#addArgument(String, boolean)
   * @param b true or false, if not specified defaults to false.
   */
  public void setQuoteHandling(Boolean b) {
    this.quoteHandling = b;
  }

  boolean quoteHandling() {
    return getQuoteHandling() != null ? getQuoteHandling().booleanValue() : false;
  }
}
