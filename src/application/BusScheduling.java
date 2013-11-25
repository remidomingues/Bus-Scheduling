package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jxl.read.biff.BiffException;
import locationsData.Bus;
import locationsData.DataMatrix;
import locationsData.Location;
import locationsData.Path;
import locationsData.Person;
import locationsData.TrafficInformation;
import presentation.GraphicalUserInterface;
import presentation.LocationNotFoundFrame;
import presentation.ProcessFrame;
import presentation.ProgressionEvent;
import presentation.ProgressionEvent.ProgressionType;
import presentation.ProgressionListener;
import antColonyOptimization.MinMaxAntSystem;
import antColonyOptimization.SolverData;
import antColonyOptimization.SolverParameters;
import dataLoader.DatabaseManager;
import dataLoader.GoogleDatabaseDistances;
import dataLoader.GoogleGeocode;
import dataLoader.GoogleService.LocationNotFoundException;
import dataLoader.GoogleService.OverQueryLimitException;
import dataLoader.GoogleService.RequestDeniedException;
import dataLoader.XLSParser;

/**
 * Permits to find a path from XLS files containing locations.
 * The path found has to pass by specified locations.
 */
public class BusScheduling extends Thread implements ProgressionInvoker
{
	/** Path of the XLS file containing the interest points location */
	// Unused
	private File INTEREST_POINTS_LOCATION_FILE = new File("data/Endereços Úteis.xls");

	/** Path of the XLS file containing the people location */
	// Unused
	private File PEOPLE_LOCATION_FILE = new File("data/Cadastros Transporte Eficiente.xls");

	/** XLS Results model */
	private File XLS_RESULTS_MODEL = new File("resources/resultsModel.xls");

	/** Path of the XLS file containing the locations data */
	private File pathsFile;

	/** Number of the XLS sheet in the paths file */
	private int pathsSheet;

	/** Path of the XLS file containing the configuration */
	private File configurationFile;

	/** XLS parser using to parse configuration, paths, people and interest locations */
	XLSParser xlsParser = new XLSParser();

	/** Array containing interest points locations parsed from the XLS files */
	@SuppressWarnings("unused")
	private Location[] interestPointsLocations;
	
	/** Array containing interest points locations parsed from the XLS files */
	private Location[] locations;

	// Unused
	/** Array containing people information and their location parsed from the XLS files */
	@SuppressWarnings("unused")
	private Person[] people;

	/** Permits to find a path as short as possible under constraints, passing by specified locations */
	private MinMaxAntSystem minMaxAntSystem;

	/** List of path sent to the solver */
	private List<Path> paths = new ArrayList<Path>();

	/** Parse long into hours under the specified format constraint */
	private static final SimpleDateFormat HOUR_FORMATER = new SimpleDateFormat("HH:mm");

	/** Constant used in order to print a schedule from midnight */
	private static final int TIMESTAMP_ADD = 3600000 * 3;

	/** Bus used in order to respect capacity, driver swap and lunch constraints */
	private Bus[] bus;

	/** File where are saved the path search results */
	private File resultsFile;

	/** Data matrix containing every distances between each location */
	private DataMatrix matrix;

	/** Graphical user interface */
	private GraphicalUserInterface userInterface;

	/** Progression events listeners */
	private List<ProgressionListener> listeners = new ArrayList<ProgressionListener>();
	
	/** Database manager use to interact with database */
	private DatabaseManager databaseManager;
	
	/** true if a Google Business subscription is owned, false else */
	private boolean subscription = false;
	
	/** Traffic coefficients */
	private TrafficInformation traffic = new TrafficInformation(6*24);
	
	/** Progression frame */
	ProcessFrame processFrame;

	/**
	 * Constructor
	 * @param subscription true if Gidion owns a Google Business subscription, false else
	 * @param configFile Configuration input file
	 * @param pathsFile Paths input file
	 * @param sheet Paths file sheet number
	 * @param resultsFile Output file
	 * @param userInterface User interface used to fire events
	 * @param processFrame Progression frame
	 * @throws FileNotFoundException If a file can't be found
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public BusScheduling(File configFile, File pathsFile, int sheet, File resultsFile,
			GraphicalUserInterface userInterface, boolean subscription, ProcessFrame processFrame) throws FileNotFoundException, ClassNotFoundException, SQLException
	{
		this.configurationFile = configFile;
		this.pathsFile = pathsFile;
		this.pathsSheet = sheet;
		this.resultsFile = resultsFile;
		this.userInterface = userInterface;
		this.databaseManager = DatabaseManager.getInstance();
		this.subscription = subscription;
		this.processFrame = processFrame;
	}

	/**
	 * Starts the program
	 */
	@Override
	public void run()
	{
		Date startTime = new Date();
		// Unused
		// try
		// {
		//		parseXLSLocations();
		// }
		// catch (BusSchedulingException e)
		// {
		// 		System.err.println(e.getMessage());
		// 		return;
		// }
		// catch (Exception e)
		// {
		// 		System.err.println("\nErro encontrado ao ler os lugar das lugares ficheiros. (Error encountered while reading interest points's locations)");
		// 		return;
		// }

		// Parse XLS configuration file
		try
		{
			parseXLSConfiguration();
		}
		catch (BusSchedulingException e)
		{
			System.err.println(e.getMessage());
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		catch (Exception e)
		{
			System.err.println("\nErro encontrado ao carregamento da configuração de " + configurationFile
					+ " ficheiro. (Error encountered " +
					" parsing configuration file)");
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		fireEvent(this, ProgressionType.SET, 1);
		
		if(isInterrupted())
			return;

		// Parse XLS paths data file
		try
		{
			parseXLSPaths();
		}
		catch (BusSchedulingException e)
		{
			System.err.println(e.getMessage());
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		catch (Exception e)
		{
			System.err.println("\nErro encontrado ao carregamento dos trajetos de " + pathsFile
					+ " ficheiro. (Error encountered while parsing paths file)");
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		fireEvent(this, ProgressionType.SET, 2);
		
		if(isInterrupted())
			return;
		
		try
		{
			buildLocationsFromPaths();
		}
		catch (BusSchedulingException e1)
		{
			System.err.println(e1.getMessage());
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}

		// Ask Google Geocoding API for locations coordinates
		try
		{
			fillLocationsCoordinates();
		}
		catch (BusSchedulingException e1)
		{
			System.err.println(e1.getMessage());
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		catch (Exception e)
		{
			System.err.println("Um erro ocorreu ao pedir côordenadas dos endereços. (An error occurred while asking for locations coordinates)");
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		fireEvent(this, ProgressionType.SET, 7);
		
		if(isInterrupted())
			return;

		try
		{
			saveCoordinates();
		}
		catch (BusSchedulingException e1)
		{
			System.err.println(e1.getMessage());
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		catch (Exception e)
		{
			System.err
					.println("\nUm erro ocorreu quando salvar côordenadas.\nSe os ficheiros for aberto, obrigado de fechar-o.\n (An error occurred while saving coordinates)");
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		fireEvent(this, ProgressionType.INCREMENT, 1);
		
		if(isInterrupted())
			return;

		// Ask Google Distance Matrix API for distances requests
		try
		{
			processGoogleDistanceRequests();
		}
		catch (BusSchedulingException e)
		{
			System.err.println(e.getMessage());
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		catch(InterruptedException e)
		{
			return;
		}
		catch (Exception e)
		{
			System.err.println("\nErro encontrado ao construir e ao processar pedidos do distância de"
					+ " Google.\nThis podem ser causados por um problema com sua conexão a "
					+ "internet.\n(Error encountered while building and processing Google "
					+ "distance requests.\nThis may be caused by a problem with yout Internet connection)");
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		fireEvent(this, ProgressionType.SET, 50);
		
		if(isInterrupted())
			return;

		// Run local search optimization and ACO searches
		try
		{
			searchBusRepartition();
		}
		catch (BusSchedulingException e)
		{
			System.err.println(e.getMessage());
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		catch(InterruptedException e)
		{
			return;
		}
		catch (Exception e)
		{
			System.err.println("\nErro encontrado ao buscar o repartition do ônibus. (Error encountered while searching bus repartition)");
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		fireEvent(this, ProgressionType.SET, 99);
		
		System.out.print(String.format("\nTempo de busca total : %s \n\n",
				longToDurationString((new Date().getTime() - startTime.getTime()) / 1000)));

		if(isInterrupted())
			return;
		
		// Print bus routes informations in an XLS workbook
		try
		{
			saveBusRoutesInformation();
		}
		catch (Exception e)
		{
			System.err
					.println("\nUm erro ocorreu ao escrever no ficheiro de saída.\nSe o ficheiro é aberto, obrigado de fechar-o. (An error occurred while writing in the output file. Please close it if this one is open)");
			fireEvent(this, ProgressionType.INTERRUPT, null);
			return;
		}
		fireEvent(this, ProgressionType.SET, 100);

		try
		{
			databaseManager.close();
		}
		catch (SQLException e) {}
		
		if (userInterface != null)
		{
			userInterface.configureOutput();
		}
	}

	/**
	 * Ask to Google the locations coordinates, parse the response and fill the coordinates in the locations
	 * @throws IOException 
	 * @throws RequestDeniedException 
	 * @throws NullPointerException 
	 * @throws BusSchedulingException 
	 * @throws Exception If an exception occurs
	 */
	private void fillLocationsCoordinates() throws NullPointerException, RequestDeniedException, IOException, BusSchedulingException, Exception
	{
		System.out.print("Construindo e processando pedidos por côordenadas dos lugares.....");
		int cpt = 1, percent = 0, tmpPercent, id;
		for (Location l : locations)
		{
			//Request for geographic coordinates
			if (!l.isGeocoded())
			{
				try
				{
					GoogleGeocode.geocode(l);
				}
				catch (OverQueryLimitException e)
				{
					throw new BusSchedulingException(
							"O número de pedidos  de coordenadas autorizado pelo dia por a API Google Geocoding foi atingido. (The Google requests"
									+ " number allowed per day has been reached)");
				}
				catch (LocationNotFoundException e)
				{
					throw new BusSchedulingException("O lugar " + l.getAddress()
							+ " não pode ser encontrado. (Not found location)");
				}
			}
			//Check if the localisation is in the database
			try
			{
				id = databaseManager.getLocationId(l);
			}
			catch(SQLException e)
			{
				try
				{
					databaseManager.close();
				}
				catch(SQLException e1) {}
				throw new BusSchedulingException("O pedido à base de dados voltou um erro. (Error encountered while requesting database)");
			}
			
			//Validation of geographic coordinates and insertion in database
			if(id == DatabaseManager.UNSET_VALUE)
			{
				if(!isGeolocalisationCorrect(l.getLatitude(), l.getLongitude()))
				{
					String message = "'" + l.getAddress() + "'\nnão pode ser encontrado. Obrigado entrar novas côordenadas.";
					LocationNotFoundFrame frame = new LocationNotFoundFrame(processFrame, message);
					
					if(frame.isCanceled())
						throw new BusSchedulingException("Tratamento parado pelo utilizador. (Process stopped by user)");
					
					l.setLatitude(frame.getLatitude());
					l.setLongitude(frame.getLongitude());
					l.setCoordinatesFilled(false);
				}
				
				try
				{
					databaseManager.insertLocation(l);
				}
				catch(SQLException e)
				{
					try
					{
						databaseManager.close();
					}
					catch(SQLException e1) {}
					throw new BusSchedulingException("A inserção do lugar " + l.getAddress()
							+ " na base de dados voltou um erro. (Error encountered while inserting this location in database)");
				}
				
				try
				{
					id = databaseManager.getLocationId(l);
				}
				catch(SQLException e)
				{
					try
					{
						databaseManager.close();
					}
					catch(SQLException e1) {}
					throw new BusSchedulingException("O pedido à base de dados voltou um erro. (Error encountered while requesting database)");
				}
			}
			
			l.setId(id);

			tmpPercent = 5 * cpt / locations.length;
			if (tmpPercent != percent)
			{
				fireEvent(this, ProgressionType.INCREMENT, tmpPercent - percent);
				percent = tmpPercent;
			}
			if(percent == 18)
				System.out.println();
			++cpt;
		}
		System.out.println("Feito.");
	}

	/**
	 * Return true if the coordinates given are located in Joinville, false else
	 * @param latitude Latitude
	 * @param longitude Longitude
	 * @return true if the coordinates given are located in Joinville, false else
	 */
	private boolean isGeolocalisationCorrect(float latitude, float longitude)
	{
		if(latitude > -26.225679)
			return false;
		if(latitude < -26.417700)
			return false;
		if(longitude > -48.669663)
			return false;
		if(longitude < -48.995132)
			return false;
		return true;
	}
	
	// Unused
	/** 
	 * Parse people and interest points locations XLS files and stock their content in a locations array
	 * @throws BusSchedulingException 
	 * @throws IOException 
	 * @throws BiffException 
	 */
	@SuppressWarnings("unused")
	private void parseXLSLocations() throws BiffException, IOException, BusSchedulingException
	{
		System.out.print("Carregamento das lugares.....");

		// Parsing of the people location file
		people = (Person[]) xlsParser.parseLocations(PEOPLE_LOCATION_FILE, true);

		// Parsing of the interest points location file
		interestPointsLocations = (Location[]) xlsParser.parseLocations(INTEREST_POINTS_LOCATION_FILE, false);

		System.out.println("Feito.");
	}

	/** 
	 * Parse configuration XLS file
	 * @throws BusSchedulingException 
	 * @throws IOException 
	 * @throws BiffException 
	 */
	private void parseXLSConfiguration() throws BiffException, IOException, BusSchedulingException
	{
		System.out.print("Carregamento da configuração.....");

		Integer[] times = new Integer[6];
		List<Bus> tmpBus = new ArrayList<Bus>();
		
		xlsParser.parseConfiguration(configurationFile, times, tmpBus, traffic);
		bus = tmpBus.toArray(new Bus[tmpBus.size()]);
		try
		{
			SolverData.setBasicAdditionalTime(times[0]);
			SolverData.setWheelchairAdditionalTime(times[1]);
			SolverData.setDriverSwapTime(times[2]);
			SolverData.setCoffeeTime(times[3]);
			SolverData.setDinnerTime(times[4]);
			SolverData.setDriverWorkTime(times[5]);
		}
		catch(IllegalArgumentException e)
		{
			throw new BusSchedulingException(e.getMessage());
		}

		System.out.println("Feito.");
	}

	/** 
	 * Parse paths data XLS file
	 * @throws BusSchedulingException 
	 * @throws IOException 
	 * @throws BiffException 
	 */
	private void parseXLSPaths() throws BiffException, IOException, BusSchedulingException
	{
		System.out.print("Carregamento dos trajetos.....");

		paths = xlsParser.parsePaths(pathsFile, pathsSheet);

		System.out.println("Feito.");
	}

	/** 
	 * Build the locations array from the paths list
	 * @throws BusSchedulingException 
	 */
	private void buildLocationsFromPaths() throws BusSchedulingException
	{
		int nbLoc = paths.size() * 2 + bus.length * 2, i = 0;
		locations = new Location[nbLoc];
		for (Path p : paths)
		{
			locations[i] = p.getOrigin();
			++i;
			locations[i] = p.getDestination();
			++i;
		}
		for (Bus b : bus)
		{
			locations[i] = b.getOrigin();
			++i;
			locations[i] = b.getDriverSwap();
			++i;
		}
	}

	/**
	 * Process Google Distance Matrix API requests
	 * @throws BusSchedulingException If an exception must be printed to the user
	 * @throws Exception If an exception occurs
	 */
	private void processGoogleDistanceRequests() throws BusSchedulingException, Exception
	{
		System.out.print("Construindo, processando e carregamento dos pedidos e respostas por distâncias e tempo entre lugares.....");
		
		try
		{
			matrix = new DataMatrix(locations);
			GoogleDatabaseDistances.fillMatrix(matrix, this, subscription);
		}
		catch (LocationNotFoundException e)
		{
			throw new BusSchedulingException("O lugar " + e.getLocation().getAddress()
					+ " não pode ser encontrado");
		}
		catch (OverQueryLimitException e)
		{
			throw new BusSchedulingException("O número de pedidos de distância autorizado pelo dia por a API Google Distância foi atingido. (The Google requests"
					+ " number allowed per day has been reached)");
		}
		catch(SQLException e)
		{
			throw new BusSchedulingException("O acesso à base dados de voltado um erro. (Error encountered while requesting database)");
		}

		System.out.println("Feito.");
	}

	/**
	 * Uses a local search and ant colony optimization algorithm in order to cut the locations
	 * given into several routes (one for each bus). These routes must respect the specified constraints.
	 * @throws BusSchedulingException 
	 * @throws InterruptedException 
	 */
	private void searchBusRepartition() throws BusSchedulingException, InterruptedException
	{
		System.out.print("Começando a busca do repartition do ônibus.....");

		SolverData data = new SolverData(matrix, paths, Arrays.asList(bus), traffic);
		minMaxAntSystem = new MinMaxAntSystem(data, new SolverParameters(matrix));
		minMaxAntSystem.setBusScheduling(this);
		minMaxAntSystem.solve();

		System.out.println("Feito.");
	}

	/**
	 * Print pieces of informations relative to the routes found by the local search
	 * @throws Exception 
	 */
	private void saveBusRoutesInformation() throws Exception
	{
		System.out.print("Salvaguarda dos resultados.....");

		xlsParser.writeResults(XLS_RESULTS_MODEL, resultsFile, bus, minMaxAntSystem.getBestPartition());

		System.out.println("Feito.");
	}

	/**
	 * Save local search results in an XLS file
	 * @throws BusSchedulingException 
	 * @throws Exception If an exception occurs
	 */
	private void saveCoordinates() throws BusSchedulingException, Exception
	{
		System.out.print("Salvaguarda dos côordenadas.....");

		xlsParser.writeConfigurationCoordinates(configurationFile, bus);
		xlsParser.writePathsCoordinates(pathsFile, paths);

		System.out.println("Feito.");
	}

	/**
	 * Return a human comprehensive description of the time received
	 * @param time The time to parse
	 * @return A description of the time
	 */
	public static String longToDurationStringNoSeconds(long time)
	{
		long hours, minutes, days;
		boolean neg = time < 0;
		if (neg)
			time *= -1;

		days = time / (3600 * 24);
		hours = time / 3600 - days * 24;
		minutes = time / 60 - hours * 60 - days * 24 * 60;
		if (days > 0)
		{
			if (neg)
				return String.format("- %d dia(s), %d hora(s), %d minuto(s)", days, hours,
						minutes);
			return String.format("%d dia(s), %d hora(s), %d minuto(s)", days, hours, minutes);
		}
		if (hours > 0)
		{
			if (neg)
			{
				return String.format("- %d hora(s), %d minuto(s)", hours, minutes);
			}
			return String.format("%d hora(s), %d minuto(s)", hours, minutes);
		}
		if (minutes > 0)
		{
			if (neg)
			{
				return String.format("- %d minuto(s)", minutes);
			}
			return String.format("%d minuto(s)", minutes);
		}
		return "";
	}

	/**
	 * Return a human comprehensive description of the time received
	 * @param time The time to parse
	 * @return A description of the time
	 */
	public static String longToDurationString(long time)
	{
		long hours, minutes, seconds, days;
		boolean neg = time < 0;
		if (neg)
			time *= -1;

		days = time / (3600 * 24);
		hours = time / 3600 - days * 24;
		minutes = time / 60 - hours * 60 - days * 24 * 60;
		seconds = time - hours * 3600 - minutes * 60 - days * 24 * 3600;
		if (days > 0)
		{
			if (neg)
				return String.format("- %d dia(s), %d hora(s), %d minuto(s), %d segundo(s)", days, hours,
						minutes, seconds);
			return String.format("%d dia(s), %d hora(s), %d minuto(s), %d segundo(s)", days, hours, minutes,
					seconds);
		}
		if (hours > 0)
		{
			if (neg)
			{
				return String.format("- %d hora(s), %d minuto(s), %d segundo(s)", hours, minutes, seconds);
			}
			return String.format("%d hora(s), %d minuto(s), %d segundo(s)", hours, minutes, seconds);
		}
		if (minutes > 0)
		{
			if (neg)
			{
				return String.format("- %d minuto(s), %d segundo(s)", minutes, seconds);
			}
			return String.format("%d minuto(s), %d segundo(s)", minutes, seconds);
		}
		if (seconds > 0)
		{
			if (neg)
			{
				return String.format("- %d segundo(s)", seconds);
			}
			return String.format("%d segundo(s)", seconds);
		}
		return "";
	}

	/**
	 * Return the hour corresponding to the time received
	 * @param time The time to parse
	 * @return The time hour
	 */
	public static String longToHourString(long time)
	{
		return HOUR_FORMATER.format(TIMESTAMP_ADD + time * 1000);
	}

	/** 
	 * Return the locations value
	 * @return The locations value
	 */
	public Location[] getLocations()
	{
		return locations;
	}

	/**
	 * Return the databaseManager value
	 * @return The databaseManager value
	 */
	public DatabaseManager getDatabaseManager()
	{
		return databaseManager;
	}

	/** 
	 * Return the antSystem 
	 * @return The antSystem 
	 */
	public MinMaxAntSystem getMinMaxAntSystem()
	{
		return minMaxAntSystem;
	}

	/** 
	 * Update the antSystem 
	 * @param antSystem The antSystem 
	 */
	public void setMinMaxAntSystem(MinMaxAntSystem antSystem)
	{
		this.minMaxAntSystem = antSystem;
	}

	/**
	 * Add a progression listener to the listeners list
	 * @param listener The listener to add
	 */
	public synchronized void addProgressionListener(ProgressionListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a progression listener from the listeners list
	 * @param listener The listener to remove
	 */
	public synchronized void removeProgressionListener(ProgressionListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Fire a progression event to listeners
	 * @param source The source object
	 * @param type Progression type
	 * @param value Progression value
	 */
	public synchronized void fireEvent(Object source, ProgressionType type, Integer value)
	{
		fireEvent(source, type, value, null);
	}

	/**
	 * Fire a progression event to listeners
	 * @param source The source object
	 * @param type Progression type
	 * @param value Progression value
	 * @param time Time to show progression
	 */
	public synchronized void fireEvent(Object source, ProgressionType type, Integer value, Long time)
	{
		ProgressionEvent event = new ProgressionEvent(this, type, value, time);
		for (ProgressionListener listener : listeners)
		{
			listener.progressionPerformed(event);
		}
	}
}