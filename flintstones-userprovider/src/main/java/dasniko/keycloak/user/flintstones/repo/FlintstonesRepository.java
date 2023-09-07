package dasniko.keycloak.user.flintstones.repo;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Niko Köbler, http://www.n-k.de, @dasniko
 */
public class FlintstonesRepository {

	private final List<FlintstoneUser> users = new ArrayList<>();

	@SneakyThrows
	public FlintstonesRepository() {
		try (InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("/flintstones.csv"))) {
			List<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines().toList();
			lines.forEach(line -> {
				String[] values = line.split(";");
				users.add(
					new FlintstoneUser(values[0], values[1], values[2], Boolean.parseBoolean(values[3]), values.length > 4 ? List.of(values[4]) : null)
				);
			});
		}
	}

	public List<FlintstoneUser> getAllUsers() {
		return users;
	}

	public int getUsersCount() {
		return users.size();
	}

	private FlintstoneUser findUserByUsernameOrEmailInternal(String username) {
		return users.stream()
			.filter(user -> user.getUsername().equalsIgnoreCase(username) || user.getEmail().equalsIgnoreCase(username))
			.findFirst().orElse(null);
	}

	public FlintstoneUser findUserByUsernameOrEmail(String username) {
		FlintstoneUser user = findUserByUsernameOrEmailInternal(username);
		return user != null ? user.clone() : null;
	}

	public List<FlintstoneUser> findUsers(String query) {
		return users.stream()
			.filter(user -> query.equalsIgnoreCase("*") || user.getUsername().contains(query) || user.getEmail().contains(query))
			.collect(Collectors.toList());
	}

	public boolean validateCredentials(String username, String password) {
		return findUserByUsernameOrEmailInternal(username).getPassword().equals(password);
	}

	public boolean updateCredentials(String username, String password) {
		findUserByUsernameOrEmailInternal(username).setPassword(password);
		return true;
	}

	public void createUser(FlintstoneUser user) {
		user.setCreated(System.currentTimeMillis());
		users.add(user);
	}

	public void updateUser(FlintstoneUser user) {
		FlintstoneUser existing = findUserByUsernameOrEmailInternal(user.getUsername());
		existing.setEmail(user.getEmail());
		existing.setFirstName(user.getFirstName());
		existing.setLastName(user.getLastName());
		existing.setEnabled(user.isEnabled());
	}

	public boolean removeUser(String username) {
		return users.removeIf(p -> p.getUsername().equals(username));
	}

}
