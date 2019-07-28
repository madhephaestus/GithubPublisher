package com.neuronrobotics.publish;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import com.neuronrobotics.bowlerstudio.scripting.PasswordManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

public class DoRelease {

	public static void main(String[] args) throws IOException {
		
		String repo = args[0];
		String organizationString = args[1];
		String tag = args[2];
		String filename = args[3];
		File artifact = new File(filename);
		if(!artifact.exists()){
			System.out.println("File does not exist! "+filename);
			return;
		}
		String artifactName = artifact.getName();
		
		System.out.println("Publishing repo="+repo+" "+"\ntag="+tag+"\nfile="+filename);
		ScriptingEngine.login();
		GitHub github = PasswordManager.getGithub();
		GHOrganization nr = github.getMyOrganizations().get(organizationString);
		nr.listRepositories();
		GHRepository repository = nr.getRepository(repo);
		if(repository==null){
			System.out.println("Repo does not exist! "+repo);
			return;
		}
		System.out.println("Found "+repository);
		GHRelease thisrelease = null;
		do{
			PagedIterable<GHRelease> releases = repository.listReleases();
			for(GHRelease rel:releases){
				if(rel.getTagName().contains(tag)){
					thisrelease=rel;
				}
			}
			if(thisrelease==null){
				System.out.println("Release does not exist, creating "+tag);
				repository.createRelease(tag);
			}
		}while(thisrelease==null);


		List<GHAsset> assets = thisrelease.getAssets();
		for(GHAsset ass:assets){
			if(ass.getName().contains(artifactName)){
				System.out.println("Artifact exists! "+ass);
				return;
			}
		}
		System.out.print("\r\nBegin uploading "+artifactName+" ... ");
		thisrelease.uploadAsset(artifact, "file");
		System.out.print(" DONE!\n");

		
	}

}
