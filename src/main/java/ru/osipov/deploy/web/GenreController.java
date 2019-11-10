package ru.osipov.deploy.web;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.osipov.deploy.models.CreateGenreR;
import ru.osipov.deploy.models.GenreInfo;
import ru.osipov.deploy.services.GenreService;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping("/v1/genres")
public class GenreController {

    private final GenreService gService;

    private static final Logger logger = LoggerFactory.getLogger(GenreController.class);

    @Autowired
    public GenreController(GenreService gs){
        this.gService = gs;
    }

    //GET: /v1/genres/{genre_name}
    //If no any name was specified -> getAll() [GET: /v1/genres, /v1/genres/]
    @GetMapping(produces = APPLICATION_JSON_UTF8_VALUE, path={"/{name}","","/"})
    public List<GenreInfo> getAllByName(@PathVariable(required = false, name= "name") String name){
        logger.info("/v1/genres");
        List<GenreInfo> genres;
        if(name == null || name.equals("")) {
            logger.info("Name was not specified. Get all.");
            genres = gService.getAllGenres();
        }
        else {
            logger.info("/v1/genres/'{}'",name);
            logger.info("Name is '{}'",name);
            genres = new ArrayList<GenreInfo>();
            genres.add(gService.getByName(name));
        }
        logger.info("Count: "+genres.size());
        return genres;
    }

    //GET: /v1/genres/remarks?r='...'
    //if no remark was specified and all too, then -> getByRemarks(null)
    //  [GET: /v1/genres/remarks, /v1/genres/remarks?r=, /v1/genres/remarks?r=&&all=, /v1/genres/remarks?all, /v1/genres/remarks?all=]
    //if no remark was specified BUT all was, then -> getAll() [GET: /v1/genres/remarks?all=true, /v1/genres/remarks?r=&&all=true]
    @GetMapping(produces = APPLICATION_JSON_UTF8_VALUE, path = "/remarks")
    public List<GenreInfo> getAllByRemarks(@RequestParam(required = false, name="r") String remarks, @RequestParam(required = false, name = "all", defaultValue = "false") Boolean all){
        List<GenreInfo> genres;
        logger.info("/v1/genres/remarks");
        if( (remarks == null || remarks.equals("")) && all){
            logger.info("Remarks is null but fetch all cause p_all = '{}' ",all);
            genres = gService.getAllGenres();
        }
        else if(remarks == null || remarks.equals("")){
            logger.info("Remarks is null. Fetch by remarks cause p_all = '{}'",all);
            genres = gService.getByRemarks(null);
        }
        else{
            logger.info("Remarks = '{}'",remarks);
            genres = gService.getByRemarks(remarks);
        }
        logger.info("Count: "+genres.size());
        return genres;
    }


    //POST: /v1/genres/update/{genre_name}?newName='...'
    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE,path={"/update/{genre}"})
    public ResponseEntity updateGenreName(@PathVariable(name = "genre") String genre, @RequestParam(name = "newName") String name){
        if(name.equals("")){
            logger.info("Empty name = '{}'",name);
            return ResponseEntity.badRequest().build();
        }
        GenreInfo g = null;
        logger.info("/v1/genres/update/'{}'",genre);
        try {
            g = gService.updateGenre(genre, name);
        }
        catch(IllegalStateException e){
            logger.info("Return json object with error message...");
            JsonObject obj = new JsonObject();
            obj.addProperty("error",e.getMessage());
            return ResponseEntity.ok(obj.toString());
        }
        return ResponseEntity.ok(g);
    }

    //POST: /v1/genres/create
    @PostMapping(consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE, path = "/create")
    public ResponseEntity createGenre(@Valid @RequestBody CreateGenreR request) {
        logger.info("/v1/genres/create");
        final URI location = gService.createGenre(request);
        return ResponseEntity.created(location).build();
    }

    //POST: /v1/genres/delete/{genre_name}
    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE, path={"/delete/{genre}","/delete/"})
    public ResponseEntity deleteGenre(@PathVariable(name = "genre")String genre){
        logger.info("/v1/genres/delete/'{}'",genre);
        GenreInfo g = null;
        try{
            g = gService.deleteGenre(genre);
        }
        catch (IllegalStateException e){
            logger.info("Return not found 404");
            //JsonObject obj = new JsonObject();
            //obj.addProperty("error",e.getMessage());
            //return ResponseEntity.ok(obj.toString());
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(g);
    }
}
